package com.qudus.docket_backend.document;

import com.google.firebase.auth.FirebaseToken;
import com.qudus.docket_backend.cloudinary.CloudinaryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/document")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/sign")
    public ResponseEntity<Map<String, Object>> handleSignUploadRequest(@AuthenticationPrincipal FirebaseToken token, @RequestBody SignRequest request) {
        if (request.familyId() == null || request.familyId().isBlank()) {
            throw new CloudinaryException("missing_family_id", "familyId is required to sign an upload", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.OK).body(documentService.getSignedUrl(request.familyId().trim(), token.getUid()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> handleConfirmUploadRequest(@AuthenticationPrincipal FirebaseToken token, @RequestBody ConfirmRequest request) {
        if (request.familyId() == null || request.familyId().isBlank()
                || request.docId() == null || request.docId().isBlank()
                || request.cloudinaryVersion() == null || request.cloudinaryVersion().isBlank()
                || request.signature() == null || request.signature().isBlank()
                || request.encryptedMetadata() == null || request.encryptedMetadata().isBlank()
                || request.kv() == null || request.kv().isBlank()) {
            throw new CloudinaryException("invalid_confirm_request", "familyId, docId, cloudinaryVersion, signature, encryptedMetadata and kv are required", HttpStatus.BAD_REQUEST);
        }
        documentService.confirmUpload(request.familyId().trim(), token.getUid(), request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Upload confirmed"));
    }
}
