package com.qudus.docket_backend.auth;

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

import java.util.List;

@Service
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;
    private final FamilyService familyService;
    public  AuthService(UserService userService, FamilyService familyService) {
        this.userService = userService;
        this.familyService = familyService;
    }

    public AuthResponse register(String userId, @NonNull AuthRequest request) {
        if(request.fullName().isEmpty() || request.email().isEmpty() || request.phone().isEmpty()) {
            throw new AuthException("Full name, Email or Phone number cannot be empty");
        }
        CreateUserRequest userRequest = new CreateUserRequest(request.fullName(), request.email(), request.phone(), false);
        CreateFamilyRequest familyRequest = new CreateFamilyRequest(request.vaultName(), userId);
        UserResponse user = userService.createUser(userId, userRequest);
        FamilyResponse family = familyService.createFamily(familyRequest, userId);
        log.info("User {} registered successfully", userId);
        return new AuthResponse(user, List.of(family));
    }
}
