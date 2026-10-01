package com.qudus.docket_backend.user;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.qudus.docket_backend.exceptions.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore db;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(Firestore db) {
        this.db = db;
    }

    public UserResponse createUser(String userId, CreateUserRequest request) {
        try {
            var docRef = db.collection("users").document(userId);
            var user = docRef.get().get();

            User parsedUser = user.toObject(User.class);

            if(user.exists()) {
               return parsedUser.toResponse();
            }

            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
            long creationMilli = userRecord.getUserMetadata().getCreationTimestamp();

            User newUser = new User(userId, request.fullName(), request.phone(), request.email(), null, Instant.ofEpochMilli(creationMilli));

            docRef.set(newUser).get();
            log.info("user created successfully for user {}", request.email());
            return newUser.toResponse();
        } catch (ExecutionException | InterruptedException | FirebaseAuthException e) {
            log.error(e.getMessage());
            throw new AuthException("Failed to create your account, please try again");
        }
    }


}
