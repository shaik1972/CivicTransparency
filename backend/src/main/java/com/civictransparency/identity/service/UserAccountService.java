package com.civictransparency.identity.service;

import com.civictransparency.identity.dto.CreateUserAccountRequest;
import com.civictransparency.identity.dto.UserAccountResponse;

import java.util.List;
import java.util.UUID;

public interface UserAccountService {

    UserAccountResponse createUser(CreateUserAccountRequest request);

    UserAccountResponse createUser(
            CreateUserAccountRequest request,
            String passwordHash);

    UserAccountResponse getUser(UUID userId);

    UserAccountResponse getUserByEmail(String email);

    UserAccountResponse getUserByUsername(String username);

    List<UserAccountResponse> getAllUsers();

    UserAccountResponse updateStatus(UUID userId, String status);

}