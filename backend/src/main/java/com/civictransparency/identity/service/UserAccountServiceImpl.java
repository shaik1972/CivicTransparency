package com.civictransparency.identity.service;

import com.civictransparency.identity.dto.CreateUserAccountRequest;
import com.civictransparency.identity.dto.UserAccountResponse;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.civictransparency.common.exception.DuplicateRegistrationException;

@Service
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

        private final UserAccountRepository userAccountRepository;

        public UserAccountServiceImpl(
                        UserAccountRepository userAccountRepository) {
                this.userAccountRepository = userAccountRepository;
        }

        @Override
        public UserAccountResponse createUser(
                        CreateUserAccountRequest request) {

                return createUser(request, null);
        }

        @Override
        public UserAccountResponse createUser(
                        CreateUserAccountRequest request,
                        String passwordHash) {

                if (userAccountRepository.existsByEmail(request.email())) {
                        throw new DuplicateRegistrationException(
                                        "An account with this email already exists.");
                }

                if (request.username() != null
                                && !request.username().isBlank()
                                && userAccountRepository.existsByUsername(
                                                request.username())) {

                        throw new DuplicateRegistrationException(
                                        "This username is already in use.");
                }

                UserAccount userAccount = new UserAccount(
                                request.email(),
                                request.status());

                if (request.username() != null
                                && !request.username().isBlank()) {

                        userAccount.setUsername(request.username());
                }

                if (passwordHash != null) {
                        userAccount.setPasswordHash(passwordHash);
                }

                UserAccount saved = userAccountRepository.save(userAccount);

                return toResponse(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public UserAccountResponse getUser(UUID userId) {

                UserAccount userAccount = userAccountRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User account not found: "
                                                                + userId));

                return toResponse(userAccount);
        }

        @Override
        @Transactional(readOnly = true)
        public UserAccountResponse getUserByEmail(String email) {

                UserAccount userAccount = userAccountRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User account not found for email: "
                                                                + email));

                return toResponse(userAccount);
        }

        @Override
        @Transactional(readOnly = true)
        public UserAccountResponse getUserByUsername(
                        String username) {

                UserAccount userAccount = userAccountRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User account not found for username: "
                                                                + username));

                return toResponse(userAccount);
        }

        @Override
        @Transactional(readOnly = true)
        public List<UserAccountResponse> getAllUsers() {

                return userAccountRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        public UserAccountResponse updateStatus(
                        UUID userId,
                        String status) {

                UserAccount userAccount = userAccountRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User account not found: "
                                                                + userId));

                userAccount.setStatus(status);

                return toResponse(
                                userAccountRepository.save(userAccount));
        }

        private UserAccountResponse toResponse(
                        UserAccount userAccount) {

                return new UserAccountResponse(
                                userAccount.getUserId(),
                                userAccount.getUsername(),
                                userAccount.getEmail(),
                                userAccount.getStatus());
        }

}