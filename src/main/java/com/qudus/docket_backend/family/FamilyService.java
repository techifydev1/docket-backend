package com.qudus.docket_backend.family;

import com.google.cloud.firestore.Firestore;
import com.qudus.docket_backend.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FamilyService {
    private final Logger log = LoggerFactory.getLogger(FamilyService.class);
    private final Firestore db;

    public FamilyService(Firestore db) {
        this.db = db;
    }

    public FamilyResponse createFamily(CreateFamilyRequest request, String userId) {
        try {
            String id = UUID.randomUUID().toString();
            var docRef = db.collection("families").document(id);
            Family newFamily = new Family(request.name(), userId, id, null, 1, Instant.now().toString(), List.of(new FamilyMember(userId, Role.OWNER, Instant.now().toString())));
            docRef.set(newFamily).get();
            return newFamily.toResponse();
        } catch (ExecutionException | InterruptedException e) {
            throw new FamilyException("Failed to create family: " + request.name(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "unknown_error");
        }
    }
}
