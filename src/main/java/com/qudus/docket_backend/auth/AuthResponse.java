package com.qudus.docket_backend.auth;

import com.qudus.docket_backend.family.FamilyResponse;
import com.qudus.docket_backend.user.UserResponse;

import java.util.List;

public record AuthResponse(UserResponse user, List<FamilyResponse> family) {
}
