package com.qudus.docket_backend.recovery;

import com.qudus.docket_backend.exceptions.AuthException;
import com.qudus.docket_backend.user.UserResponse;
import com.qudus.docket_backend.user.UserService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecoveryService {
    private final Logger log = LoggerFactory.getLogger(RecoveryService.class);
    private final UserService userService;

    public RecoveryService(UserService userService) {
        this.userService = userService;
    }

    public UserResponse saveKeyBackup(String userId, @NonNull RecoveryRequest request) {
        if (request.keyBackup() == null || request.keyBackup().isBlank()) {
            throw new AuthException("Your recovery key is missing, please try again");
        }
        log.info("Saving recovery key backup for user {}", userId);
        return userService.updateKeyBackup(userId, request.keyBackup());
    }
}
