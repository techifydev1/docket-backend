package com.qudus.docket_backend.user;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.qudus.docket_backend.exceptions.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore db;
    private Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(Firestore db) {
        this.db = db;
    }

    public UserResponse createUser(String userId, CreateUserRequest request) {
        try {
            var docRef = db.collection("users").document(userId);
            var user = docRef.get().get();

            if(user.exists()) {
                return new UserResponse(user.getString("fullName"), user.getString("id"), user.getString("email"), user.getString("phone"), user.getString("profilePic"), user.getLong("createdAt"));
            }

            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
            long creationMilli = userRecord.getUserMetadata().getCreationTimestamp();

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("userId", userId);
            userProfile.put("email", request.email());
            userProfile.put("fullName", request.fullName());
            userProfile.put("phone", request.phone());
            userProfile.put("biometricsEnabled", request.biometricsEnabled());
            userProfile.put("createdAt", creationMilli);


            docRef.set(userProfile).get();
            log.info("user created successfully for user {}", request.email());
            return new UserResponse(request.fullName(), userId, request.email(), request.phone(), null, creationMilli);
        } catch (ExecutionException | InterruptedException | FirebaseAuthException e) {
            log.error(e.getMessage());
            throw new AuthException("Failed to create your account, please try again");
        }
    }


}
