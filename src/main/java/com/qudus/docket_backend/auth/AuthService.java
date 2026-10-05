package com.qudus.docket_backend.auth;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.qudus.docket_backend.email.EmailService;
import com.qudus.docket_backend.exceptions.AuthException;
import com.qudus.docket_backend.family.CreateFamilyRequest;
import com.qudus.docket_backend.family.FamilyResponse;
import com.qudus.docket_backend.family.FamilyService;
import com.qudus.docket_backend.user.CreateUserRequest;
import com.qudus.docket_backend.user.UserResponse;
import com.qudus.docket_backend.user.UserService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;
    private final FamilyService familyService;
    private final EmailService emailService;
    private static final SecureRandom secureRandom = new SecureRandom();
    private final VerificationService verificationService;
    private final Firestore db;

    public AuthService(UserService userService, FamilyService familyService, EmailService emailService, VerificationService verificationService, Firestore db) {
        this.userService = userService;
        this.familyService = familyService;
        this.emailService = emailService;
        this.verificationService = verificationService;
        this.db = db;
    }

    public AuthResponse register(String userId, @NonNull AuthRequest request) {
        if (request.fullName().isEmpty() || request.email().isEmpty() || request.phone().isEmpty()) {
            throw new AuthException("Full name, Email or Phone number cannot be empty");
        }
        CreateUserRequest userRequest = new CreateUserRequest(request.fullName(), request.email(), request.phone(), false);
        CreateFamilyRequest familyRequest = new CreateFamilyRequest(request.vaultName(), userId);
        UserResponse user = userService.createUser(userId, userRequest);
        FamilyResponse family = familyService.createFamily(familyRequest, userId);
        generateACodeAndSendEmail(request.email());
        log.info("User {} registered successfully", userId);
        return new AuthResponse(user, List.of(family));
    }

    private void generateACodeAndSendEmail(@NonNull String email) {
        String code = generate6DigitRandomCode();
        verificationService.saveVerificationCode(email, code);
        String content = """
                <h1>Welcome to Docket!</h1>
                <p>Here's your 6 digit verification code</p>
                
                <p> %s </p>
                
                <p>Please note that this code expires in 15 minutes.</p>
                
                <p>Best regards,</p>
                <p>Docket team.</p>
                """.formatted(code);
        emailService.sendEmail(email, content);
    }

    public Map<String, String> requestEmailVerificationCode(@NonNull String userId) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
            generateACodeAndSendEmail(userRecord.getEmail());
            return Map.of("message", "Email verification sent successfully");
        } catch (FirebaseAuthException e) {
            log.error("Unable to send user a verification code user: {}", userId);
            throw new AuthException("Unable to send a verification code, please try again in 2 mins");
        }
    }

    public Map<String, String> verifyEmail(String userId, String email, @NonNull VerifyEmailRequest request) {
        try {
            boolean isValid = verificationService.isValid(email, request.code());
            if (!isValid) {
                throw new AuthException("Expired or invalid verification code");
            }
            var docRef = db.collection("users").document(userId);
            UserRecord.UpdateRequest req = new UserRecord.UpdateRequest(userId);
            req.setEmailVerified(true);
            FirebaseAuth.getInstance().updateUser(req);
            docRef.update("emailVerified", true).get();
            log.info("Email verified for user: {}", userId);
            return Map.of("message", "Email verified successfully");
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to verify user: {}", userId);
            throw new AuthException("We're unable to verify your email, please try again");
        } catch (FirebaseAuthException e) {
            log.error("Failed to verify user from firebase: {}", e.getMessage());
            throw new AuthException("We're unable to verify your email, please try again");
        }
    }

    public static String generate6DigitRandomCode() {
        int number = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(number);
    }
}
