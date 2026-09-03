package com.civictransparency.auth.service;

import com.civictransparency.auth.dto.LoginRequest;
import com.civictransparency.auth.dto.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);
}
