package com.civictransparency.auth.service;

import com.civictransparency.auth.dto.LoginRequest;
import com.civictransparency.auth.dto.LoginResponse;
import com.civictransparency.auth.exception.InvalidCredentialsException;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.authority.repository.AuthorityUserRepository;
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
import java.util.UUID;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final AuthorityUserRepository authorityUserRepository;
    private final PasswordHashingService passwordHashingService;
    private final AccessTokenService accessTokenService;
    private final long tokenTtlMinutes;
    private final long refreshTokenTtlMinutes;

    public AuthenticationServiceImpl(
            UserAccountRepository userAccountRepository,
            UserRoleRepository userRoleRepository,
            CitizenProfileRepository citizenProfileRepository,
            AuthorityUserRepository authorityUserRepository,
            PasswordHashingService passwordHashingService,
            AccessTokenService accessTokenService,
            @Value("${civic.auth.token-ttl-minutes}") long tokenTtlMinutes,
            @Value("${civic.auth.refresh-ttl-minutes:10080}") long refreshTokenTtlMinutes) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.authorityUserRepository = authorityUserRepository;
        this.passwordHashingService = passwordHashingService;
        this.accessTokenService = accessTokenService;
        this.tokenTtlMinutes = tokenTtlMinutes;
        this.refreshTokenTtlMinutes = refreshTokenTtlMinutes;
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

        List<String> roles = userRoleRepository.findByIdUserId(user.getUserId())
                .stream()
                .map(userRole -> userRole.getRole().getCode())
                .sorted()
                .toList();
        CitizenProfile profile = citizenProfileRepository.findByUserUserId(user.getUserId()).orElse(null);
        boolean systemAdmin = roles.contains("SYSTEM_ADMIN");
        boolean authorityUser = roles.stream()
                .anyMatch(role -> role.equals("AUTHORITY_ADMIN") || role.equals("AUTHORITY_OFFICER"))
                && isActiveAuthorityUser(user.getUserId());
        if (profile == null && !systemAdmin && !authorityUser) {
            throw new InvalidCredentialsException();
        }
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(tokenTtlMinutes);

        user.setLastLoginAt(OffsetDateTime.now());
        userAccountRepository.save(user);

        OffsetDateTime refreshExpiresAt = OffsetDateTime.now().plusMinutes(refreshTokenTtlMinutes);
        String refreshToken = accessTokenService.issueRefresh(user.getUserId(), roles, refreshExpiresAt);

        return new LoginResponse(
                accessTokenService.issue(user.getUserId(), roles, expiresAt),
                refreshToken,
                "Bearer",
                expiresAt,
                user.getUserId(),
                profile != null ? profile.getCitizenId() : null,
                user.getUsername(),
                profile != null ? profile.getPublicAlias() : null,
                roles);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
                UUID userId = accessTokenService.verifyRefresh(refreshToken);
        List<String> tokenRoles = accessTokenService.getRoles(refreshToken);

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);
        if (!ACTIVE_STATUS.equals(user.getStatus())) {
            throw new InvalidCredentialsException();
        }
        List<String> roles = userRoleRepository.findByIdUserId(userId)
                .stream()
                .map(userRole -> userRole.getRole().getCode())
                .sorted()
                .toList();
        if (roles.isEmpty() || !roles.containsAll(tokenRoles)) {
            throw new InvalidCredentialsException();
        }
        CitizenProfile profile = citizenProfileRepository.findByUserUserId(userId).orElse(null);
        boolean systemAdmin = roles.contains("SYSTEM_ADMIN");
        boolean authorityUser = roles.stream()
                .anyMatch(role -> role.equals("AUTHORITY_ADMIN") || role.equals("AUTHORITY_OFFICER"))
                && isActiveAuthorityUser(userId);
        if (profile == null && !systemAdmin && !authorityUser) {
            throw new InvalidCredentialsException();
        }

        OffsetDateTime accessExpiresAt = OffsetDateTime.now().plusMinutes(tokenTtlMinutes);
        OffsetDateTime refreshExpiresAt = OffsetDateTime.now().plusMinutes(refreshTokenTtlMinutes);
        String newAccessToken = accessTokenService.issue(userId, roles, accessExpiresAt);
        String newRefreshToken = accessTokenService.issueRefresh(userId, roles, refreshExpiresAt);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                accessExpiresAt,
                user.getUserId(),
                profile != null ? profile.getCitizenId() : null,
                user.getUsername(),
                profile != null ? profile.getPublicAlias() : null,
                roles);
    }

    private Optional<UserAccount> findUser(String identifier) {
        if (identifier.contains("@")) {
            return userAccountRepository.findByEmail(identifier.toLowerCase(Locale.ROOT));
        }
        return userAccountRepository.findByUsername(identifier);
    }

    private boolean isActiveAuthorityUser(UUID userId) {
        return authorityUserRepository.findByUserAccount_UserId(userId)
                .map(authorityUser -> authorityUser.getDepartment())
                .filter(department -> "ACTIVE".equals(department.getStatus()))
                .map(department -> "ACTIVE".equals(department.getAuthority().getStatus()))
                .orElse(false);
    }
}
