package com.qudus.docket_backend.recovery;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.qudus.docket_backend.auth.AuthService;
import com.qudus.docket_backend.auth.VerificationService;
import com.qudus.docket_backend.email.EmailService;
import com.qudus.docket_backend.exceptions.AuthException;
import com.qudus.docket_backend.user.UserResponse;
import com.qudus.docket_backend.user.UserService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RecoveryService {
    private final Logger log = LoggerFactory.getLogger(RecoveryService.class);
    private final UserService userService;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private static final String RECOVERY_PREFIX = "recovery:";

    public RecoveryService(UserService userService, VerificationService verificationService, EmailService emailService) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.emailService = emailService;
    }

    public UserResponse saveKeyBackup(String userId, @NonNull RecoveryRequest request) {
        if (request.keyBackup() == null || request.keyBackup().isBlank()) {
            throw new AuthException("Your recovery key is missing, please try again");
        }
        log.info("Saving recovery key backup for user {}", userId);
        return userService.updateKeyBackup(userId, request.keyBackup());
    }

    public Map<String, String> requestRecoveryCode(String userId) {
        String email = getEmail(userId);
        if (getKeyBackup(userId) == null) {
            throw new AuthException("We couldn't find a recovery key for this account");
        }
        String code = AuthService.generate6DigitRandomCode();
        verificationService.saveVerificationCode(RECOVERY_PREFIX + email, code);
        String content = """
                <h1>Docket account recovery</h1>
                <p>Here's your 6 digit recovery code</p>

                <p> %s </p>

                <p>Please note that this code expires in 15 minutes.</p>

                <p>Best regards,</p>
                <p>Docket team.</p>
                """.formatted(code);
        emailService.sendEmail(email, "Recover your Docket account", content);
        log.info("Recovery code sent to user {}", userId);
        return Map.of("message", "Recovery code sent successfully");
    }

    public RecoveryResponse confirmRecovery(String userId, @NonNull RecoveryConfirmRequest request) {
        String email = getEmail(userId);
        if (request.code() == null || !verificationService.isValid(RECOVERY_PREFIX + email, request.code())) {
            throw new AuthException("Expired or invalid recovery code");
        }
        String keyBackup = getKeyBackup(userId);
        if (keyBackup == null) {
            throw new AuthException("We couldn't find a recovery key for this account");
        }
        log.info("Recovery key handed out for user {}", userId);
        return new RecoveryResponse(keyBackup);
    }

    private String getKeyBackup(String userId) {
        String keyBackup = userService.getUser(userId).keyBackup();
        return (keyBackup == null || keyBackup.isBlank()) ? null : keyBackup;
    }

    private String getEmail(String userId) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
            return userRecord.getEmail();
        } catch (FirebaseAuthException e) {
            log.error("Unable to read recovery email for user: {}", userId);
            throw new AuthException("We're unable to recover your account, please try again");
        }
    }
}
