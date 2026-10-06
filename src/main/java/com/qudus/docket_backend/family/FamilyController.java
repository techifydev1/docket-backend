package com.qudus.docket_backend.family;

import com.google.firebase.auth.FirebaseToken;
import com.qudus.docket_backend.user.UserResponse;
import com.qudus.docket_backend.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/family")
public class FamilyController {
    private final FamilyService familyService;
    private final UserService userService;


    public FamilyController(FamilyService familyService, UserService userService) {
        this.familyService = familyService;
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<FamilyResponse>> handleGetFamilyRequest(@AuthenticationPrincipal FirebaseToken token) {
        String userId = token.getUid();
        return ResponseEntity.status(HttpStatus.OK).body(familyService.getFamilies(userId));
    }

    @PostMapping
    public ResponseEntity<FamilyResponse> handleCreateFamilyRequest(@AuthenticationPrincipal FirebaseToken token, @RequestBody CreateFamilyBody body) {
        String userId = token.getUid();
        if (body.name() == null || body.name().isBlank()) {
            throw new FamilyException("Family name cannot be empty", HttpStatus.BAD_REQUEST.value(), "invalid_name");
        }
        if (body.wrappedFamilyKey() == null || body.wrappedFamilyKey().isBlank()) {
            throw new FamilyException("Your encryption key is missing, please try again", HttpStatus.BAD_REQUEST.value(), "missing_key");
        }
        UserResponse owner = userService.getUser(userId);
        CreateFamilyRequest request = new CreateFamilyRequest(body.name().trim(), owner.fullName(), body.wrappedFamilyKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(familyService.createFamily(request, userId));
    }
}
