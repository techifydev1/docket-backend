package com.qudus.docket_backend.invitation;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.qudus.docket_backend.email.EmailService;
import com.qudus.docket_backend.family.Family;
import com.qudus.docket_backend.family.FamilyMember;
import com.qudus.docket_backend.family.FamilyResponse;
import com.qudus.docket_backend.family.Role;
import com.qudus.docket_backend.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class InvitationService {

    private final Firestore db;
    private final Logger log = LoggerFactory.getLogger(InvitationService.class);
    private final EmailService emailService;

    public InvitationService(Firestore db, EmailService emailService) {
        this.db = db;
        this.emailService = emailService;
    }

    public Map<String, Object> initiateInvite(String inviteeEmail, String familyId, String userId) {
        try {
            var familySnapShot = db.collection("families").document(familyId).get().get();
            if(!familySnapShot.exists()) throw new InvitationException("This family vault no longer exists", HttpStatus.NOT_FOUND, "family_not_found");
            Family family = familySnapShot.toObject(Family.class);
            if(family.getMemberIds() == null || !family.getMemberIds().contains(userId)) throw new InvitationException("You are not a member of this family", HttpStatus.FORBIDDEN, "not_in_family");

            var usersRef = db.collection("users");
            var snapshotFuture = usersRef.whereEqualTo("email", inviteeEmail).limit(1).get();
            var snapshot = snapshotFuture.get();
            if(snapshot.isEmpty()) throw new InvitationException("No user for this email, please check and try again", HttpStatus.BAD_REQUEST, "email_invalid");
            User invitee = snapshot.getDocuments().getFirst().toObject(User.class);

            Map<String, Object> result = new HashMap<>();
            result.put("publicKey", invitee.getPublicKey());
            result.put("keyVersion", family.getKeyVersion());
            return result;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error fetching invitee info with email: {}, with error: {}", inviteeEmail, e.getMessage());
            throw new InvitationException("An unexpected error happened", HttpStatus.INTERNAL_SERVER_ERROR, "server_error");
        }
    }

    public void invite(String inviteeEmail, String userId, String inviteePublicKey, String wrappedFamilyKey, String familyId) {
        try {
            var familySnapShot = db.collection("families").document(familyId).get().get();
            if(!familySnapShot.exists()) throw new InvitationException("This family vault no longer exists", HttpStatus.NOT_FOUND, "family_not_found");
            Family family = familySnapShot.toObject(Family.class);
            if(family.getMemberIds() == null || !family.getMemberIds().contains(userId)) throw new InvitationException("You are not a member of this family", HttpStatus.FORBIDDEN, "not_in_family");

            var usersRef = db.collection("users");
            var userSnapShotFuture = usersRef.whereEqualTo("email", inviteeEmail).limit(1).get();
            var userSnapShot = userSnapShotFuture.get();
            if(userSnapShot.isEmpty() || !userSnapShot.getDocuments().getFirst().toObject(User.class).getPublicKey().equals(inviteePublicKey)) throw new InvitationException("Invalid invitee email or publicKey", HttpStatus.BAD_REQUEST, "email_invalid");
            User invitee = userSnapShot.getDocuments().getFirst().toObject(User.class);
            if(userId.equals(invitee.getUserId())) throw new InvitationException("You cannot invite yourself", HttpStatus.BAD_REQUEST, "cannot_invite_self");
            if(family.getMemberIds().contains(invitee.getUserId())) throw new InvitationException("This user is already a member of the family", HttpStatus.BAD_REQUEST, "already_member");

            User inviter = usersRef.document(userId).get().get().toObject(User.class);

            Map<String, Object> inviteInfo = new HashMap<>();
            inviteInfo.put("inviteeUserId", invitee.getUserId());
            inviteInfo.put("inviteeEmail", invitee.getEmail());
            inviteInfo.put("inviterUserId", userId);
            inviteInfo.put("inviterEmail", inviter.getEmail());
            inviteInfo.put("wrappedFamilyKey", wrappedFamilyKey);
            inviteInfo.put("keyVersion", family.getKeyVersion());
            inviteInfo.put("status", InvitationStatus.PENDING.name());
            inviteInfo.put("familyId", familyId);
            inviteInfo.put("familyName", family.getName());
            inviteInfo.put("createdAt", Instant.now().toString());

            var invitationRef = db.collection("invitations").document(familyId + "_" + invitee.getUserId());
            invitationRef.set(inviteInfo).get();

            emailService.sendEmail(invitee.getEmail(), "You've been invited to a Docket family vault", buildInviteEmail(inviter, family));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error inviting user with email: {}, with error {}, from user: {}", inviteeEmail, e.getMessage(), userId);
            throw new InvitationException("An unknown error occurred while inviting inviteeEmail", HttpStatus.INTERNAL_SERVER_ERROR, "unknown_error");
        }
    }

    public List<InvitationResponse> getInvitations(String userId) {
        try {
            var snapshot = db.collection("invitations").whereEqualTo("inviteeUserId", userId).get().get();
            return snapshot.getDocuments().stream()
                    .filter(doc -> InvitationStatus.PENDING.name().equals(doc.getString("status")))
                    .map(doc -> new InvitationResponse(
                            doc.getId(),
                            doc.getString("familyId"),
                            doc.getString("familyName"),
                            doc.getString("inviterEmail"),
                            doc.getString("status"),
                            doc.getString("createdAt")))
                    .toList();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error fetching invitations for user: {}, with error: {}", userId, e.getMessage());
            throw new InvitationException("We couldn't load your invitations, please try again", HttpStatus.INTERNAL_SERVER_ERROR, "server_error");
        }
    }

    public FamilyResponse acceptInvitation(String invitationId, String userId) {
        var invitationRef = db.collection("invitations").document(invitationId);
        try {
            DocumentSnapshot invitationSnapshot = invitationRef.get().get();
            if(!invitationSnapshot.exists()) throw new InvitationException("Invitation not found", HttpStatus.NOT_FOUND, "invitation_not_found");
            if(!userId.equals(invitationSnapshot.getString("inviteeUserId"))) throw new InvitationException("This invitation is not yours", HttpStatus.FORBIDDEN, "not_invitee");

            String familyId = invitationSnapshot.getString("familyId");
            String wrappedFamilyKey = invitationSnapshot.getString("wrappedFamilyKey");
            Long keyVersion = invitationSnapshot.getLong("keyVersion");
            var familyRef = db.collection("families").document(familyId);
            User invitee = db.collection("users").document(userId).get().get().toObject(User.class);

            db.runTransaction(transaction -> {
                DocumentSnapshot currentInvitation = transaction.get(invitationRef).get();
                if(!InvitationStatus.PENDING.name().equals(currentInvitation.getString("status"))) throw new InvitationException("This invitation is no longer pending", HttpStatus.BAD_REQUEST, "invitation_not_pending");

                DocumentSnapshot familySnapshot = transaction.get(familyRef).get();
                if(!familySnapshot.exists()) throw new InvitationException("This family vault no longer exists", HttpStatus.NOT_FOUND, "family_not_found");
                Family family = familySnapshot.toObject(Family.class);
                if(keyVersion == null || family.getKeyVersion() != keyVersion) throw new InvitationException("This invitation is out of date, please ask to be invited again", HttpStatus.CONFLICT, "stale_invitation");
                if(family.getMemberIds() != null && family.getMemberIds().contains(userId)) throw new InvitationException("You are already a member of this family", HttpStatus.BAD_REQUEST, "already_member");

                FamilyMember member = new FamilyMember(invitee.getFullName(), userId, Role.MEMBER, Instant.now().toString(), invitee.getProfilePic());
                family.addMember(userId, keyVersion.intValue(), wrappedFamilyKey, member);
                transaction.set(familyRef, family);
                transaction.update(invitationRef, "status", InvitationStatus.ACCEPTED.name(), "acceptedAt", Instant.now().toString());
                return null;
            }).get();

            return familyRef.get().get().toObject(Family.class).toResponse(userId);
        } catch (ExecutionException | InterruptedException e) {
            if(e.getCause() instanceof InvitationException invitationException) throw invitationException;
            log.error("Error accepting invitation: {} for user: {}, with error: {}", invitationId, userId, e.getMessage());
            throw new InvitationException("We couldn't accept this invitation, please try again", HttpStatus.INTERNAL_SERVER_ERROR, "unknown_error");
        }
    }

    public void declineInvitation(String invitationId, String userId) {
        try {
            var invitationRef = db.collection("invitations").document(invitationId);
            DocumentSnapshot snapshot = invitationRef.get().get();
            if(!snapshot.exists()) throw new InvitationException("Invitation not found", HttpStatus.NOT_FOUND, "invitation_not_found");
            if(!userId.equals(snapshot.getString("inviteeUserId"))) throw new InvitationException("This invitation is not yours", HttpStatus.FORBIDDEN, "not_invitee");
            if(!InvitationStatus.PENDING.name().equals(snapshot.getString("status"))) throw new InvitationException("This invitation is no longer pending", HttpStatus.BAD_REQUEST, "invitation_not_pending");
            invitationRef.update("status", InvitationStatus.DECLINED.name(), "declinedAt", Instant.now().toString()).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error declining invitation: {} for user: {}, with error: {}", invitationId, userId, e.getMessage());
            throw new InvitationException("We couldn't decline this invitation, please try again", HttpStatus.INTERNAL_SERVER_ERROR, "unknown_error");
        }
    }

    private String buildInviteEmail(User inviter, Family family) {
        return """
                <h1>You've been invited to Docket!</h1>
                <p>%s invited you to join the "%s" family vault.</p>

                <p>Open the Docket app and accept the invitation to gain access.</p>

                <p>Best regards,</p>
                <p>Docket team.</p>
                """.formatted(inviter.getFullName(), family.getName());
    }
}
