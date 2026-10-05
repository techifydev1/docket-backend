package com.qudus.docket_backend.family;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Service
public class FamilyService {
    private final Logger log = LoggerFactory.getLogger(FamilyService.class);
    private final Firestore db;

    public FamilyService(Firestore db) {
        this.db = db;
    }

    public FamilyResponse createFamily(CreateFamilyRequest request, String userId) {
        try {
            String id = UUID.randomUUID().toString();
            var docRef = db.collection("families").document(id);
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
            Family newFamily = new Family(request.name(), userId, id, null, 1, Instant.now().toString(), List.of(new FamilyMember(request.ownerName(), userId, Role.OWNER, Instant.now().toString(), userRecord.getPhotoUrl())), List.of(userId));
            docRef.set(newFamily).get();
            log.info("New family created for user {}", userId);
            return newFamily.toResponse();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to create family for user {} with error: {}", userId, e.getMessage());
            throw new FamilyException("Failed to create family: " + request.name(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "server_error");
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FamilyResponse> getFamilies(String userId) {
        try {
            var querySnapShot = db.collection("families").whereArrayContains("memberIds", userId).get().get();
            return querySnapShot.getDocuments().stream().map(doc -> doc.toObject(Family.class).toResponse()).toList();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to get families for user: {} with error: {}", userId, e.getMessage());
            throw new FamilyException("Failed to fetch your families", HttpStatus.INTERNAL_SERVER_ERROR.value(), "server_error");
        }
    }
}
