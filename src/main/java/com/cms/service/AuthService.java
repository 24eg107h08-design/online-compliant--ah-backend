package com.cms.service;

import com.cms.dto.request.LoginRequest;
import com.cms.dto.request.RegisterRequest;
import com.cms.dto.response.AuthResponse;
import com.cms.entity.User;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    User getCurrentAuthenticatedUser();
}
