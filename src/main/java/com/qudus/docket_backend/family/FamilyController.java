package com.qudus.docket_backend.family;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/family")
public class FamilyController {
    private final FamilyService familyService;


    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<FamilyResponse>> handleGetFamilyRequest(@AuthenticationPrincipal FirebaseToken token) {
        String userId = token.getUid();
        return ResponseEntity.status(HttpStatus.OK).body(familyService.getFamilies(userId));
    }
}
