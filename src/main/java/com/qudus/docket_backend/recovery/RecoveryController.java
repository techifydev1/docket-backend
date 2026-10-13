package com.qudus.docket_backend.recovery;

import com.google.firebase.auth.FirebaseToken;
import com.qudus.docket_backend.user.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recovery")
public class RecoveryController {
    private final RecoveryService recoveryService;

    public RecoveryController(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @PostMapping("/backup")
    public ResponseEntity<UserResponse> handleSaveKeyBackup(@AuthenticationPrincipal FirebaseToken token, @RequestBody RecoveryRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(recoveryService.saveKeyBackup(token.getUid(), request));
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> handleRequestRecovery(@AuthenticationPrincipal FirebaseToken token) {
        return ResponseEntity.status(HttpStatus.OK).body(recoveryService.requestRecoveryCode(token.getUid()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<RecoveryResponse> handleConfirmRecovery(@AuthenticationPrincipal FirebaseToken token, @RequestBody RecoveryConfirmRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(recoveryService.confirmRecovery(token.getUid(), request));
    }
}
