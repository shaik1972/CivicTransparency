package com.civictransparency.auth.service;

import com.civictransparency.auth.dto.LoginRequest;
import com.civictransparency.auth.dto.LoginResponse;
import com.civictransparency.auth.exception.InvalidCredentialsException;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.identity.repository.UserRoleRepository;
import com.civictransparency.security.PasswordHashingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final PasswordHashingService passwordHashingService;
    private final AccessTokenService accessTokenService;
    private final long tokenTtlMinutes;

    public AuthenticationServiceImpl(
            UserAccountRepository userAccountRepository,
            UserRoleRepository userRoleRepository,
            CitizenProfileRepository citizenProfileRepository,
            PasswordHashingService passwordHashingService,
            AccessTokenService accessTokenService,
            @Value("${civic.auth.token-ttl-minutes}") long tokenTtlMinutes) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.passwordHashingService = passwordHashingService;
        this.accessTokenService = accessTokenService;
        this.tokenTtlMinutes = tokenTtlMinutes;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount user = findUser(request.identifier().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!ACTIVE_STATUS.equals(user.getStatus())
                || user.getPasswordHash() == null
                || !passwordHashingService.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        CitizenProfile profile = citizenProfileRepository.findByUserUserId(user.getUserId())
                .orElseThrow(InvalidCredentialsException::new);
        List<String> roles = userRoleRepository.findByIdUserId(user.getUserId())
                .stream()
                .map(userRole -> userRole.getRole().getCode())
                .sorted()
                .toList();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(tokenTtlMinutes);

        user.setLastLoginAt(OffsetDateTime.now());
        userAccountRepository.save(user);

        return new LoginResponse(
                accessTokenService.issue(user.getUserId(), roles, expiresAt),
                "Bearer",
                expiresAt,
                user.getUserId(),
                profile.getCitizenId(),
                user.getUsername(),
                profile.getPublicAlias(),
                roles);
    }

    private Optional<UserAccount> findUser(String identifier) {
        if (identifier.contains("@")) {
            return userAccountRepository.findByEmail(identifier.toLowerCase(Locale.ROOT));
        }
        return userAccountRepository.findByUsername(identifier);
    }
}
