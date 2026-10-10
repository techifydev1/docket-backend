package com.qudus.docket_backend.invitation;

import com.google.firebase.auth.FirebaseToken;
import com.qudus.docket_backend.family.FamilyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitation")
public class InvitationController {
    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> handleInitiateInvite(@AuthenticationPrincipal FirebaseToken token, @RequestBody InitiateInviteRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(invitationService.initiateInvite(request.inviteeEmail(), request.familyId(), token.getUid()));
    }

    @PostMapping
    public ResponseEntity<Void> handleInvite(@AuthenticationPrincipal FirebaseToken token, @RequestBody InviteRequest request) {
        invitationService.invite(request.inviteeEmail(), token.getUid(), request.inviteePublicKey(), request.wrappedFamilyKey(), request.familyId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<InvitationResponse>> handleGetInvitations(@AuthenticationPrincipal FirebaseToken token) {
        return ResponseEntity.status(HttpStatus.OK).body(invitationService.getInvitations(token.getUid()));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<FamilyResponse> handleAcceptInvitation(@AuthenticationPrincipal FirebaseToken token, @PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(invitationService.acceptInvitation(id, token.getUid()));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> handleDeclineInvitation(@AuthenticationPrincipal FirebaseToken token, @PathVariable String id) {
        invitationService.declineInvitation(id, token.getUid());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
