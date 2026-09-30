package com.qudus.docket_backend.user;

import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Firestore db;

    public UserService(Firestore db) {
        this.db = db;
    }

    public UserResponse createUser(user) {}


}
