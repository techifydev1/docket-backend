package com.qudus.docket_backend.auth;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> handleRegisterRequest(@RequestBody AuthRequest request, @AuthenticationPrincipal FirebaseToken token) {
        String userId = token.getUid();
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userId, request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> handleVerificationRequest(@AuthenticationPrincipal FirebaseToken token, @RequestBody VerifyEmailRequest request) {
        String userId = token.getUid();
        String email = token.getEmail();
        return ResponseEntity.status(HttpStatus.OK).body(authService.verifyEmail(userId, email, request));
    }

    @PostMapping("/send-verification-email")
    public ResponseEntity<Map<String, String>> handleSendEmailVerificationCode(@AuthenticationPrincipal FirebaseToken token) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.requestEmailVerificationCode(token.getUid()));
    }
}
