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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore db;
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final FirebaseAuth firebaseAuth;

    public UserService(Firestore db, FirebaseAuth firebaseAuth) {
        this.db = db; this.firebaseAuth = firebaseAuth;
    }

    public UserResponse createUser(String userId, CreateUserRequest request) {
        try {
            var docRef = db.collection("users").document(userId);
            var user = docRef.get().get();

            if(user.exists()) {
                User parsedUser = user.toObject(User.class);
               return parsedUser.toResponse();
            }

            UserRecord userRecord = firebaseAuth.getUser(userId);
            long creationMilli = userRecord.getUserMetadata().getCreationTimestamp();

            User newUser = new User(userId, request.fullName(), request.phone(), request.email(), null, Instant.ofEpochMilli(creationMilli), false, request.publicKey());

            docRef.set(newUser).get();
            log.info("user created successfully for user {}", request.email());
            return newUser.toResponse();
        } catch (ExecutionException | InterruptedException | FirebaseAuthException e) {
            log.error(e.getMessage());
            throw new AuthException("Failed to create your account, please try again");
        }
    }

    public UserResponse getUser(String userId) {
        try {
            var docRef = db.collection("users").document(userId);
            var user = docRef.get().get();

            if(!user.exists()) {
                throw new NoUserException("You are not authorized");
            }

            User parsedUser = user.toObject(User.class);
            return Objects.requireNonNull(parsedUser).toResponse();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Unauthorized user tried to access a user: userId: {}", userId);
            throw new NoUserException("You're not logged in, please login and try again");
        }
    }


}
