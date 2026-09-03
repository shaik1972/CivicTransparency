package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenRegistrationRequest;
import com.civictransparency.citizen.dto.CitizenRegistrationResponse;
import com.civictransparency.citizen.dto.CreateCitizenAddressRequest;
import com.civictransparency.citizen.dto.CreateCitizenContactRequest;
import com.civictransparency.citizen.dto.CreateCitizenProfileRequest;
import com.civictransparency.citizen.dto.CitizenProfileResponse;
import com.civictransparency.identity.dto.CreateUserAccountRequest;
import com.civictransparency.identity.dto.RoleResponse;
import com.civictransparency.identity.dto.UserAccountResponse;
import com.civictransparency.identity.dto.UserRoleResponse;
import com.civictransparency.identity.service.RoleService;
import com.civictransparency.identity.service.UserAccountService;
import com.civictransparency.identity.service.UserRoleService;
import com.civictransparency.security.PasswordHashingService;
import com.civictransparency.common.exception.InvalidRegistrationDataException;
import com.civictransparency.security.PiiEncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CitizenRegistrationServiceImpl
        implements CitizenRegistrationService {

    private static final String CITIZEN_ROLE = "CITIZEN";

    private static final String ACCOUNT_STATUS = "ACTIVE";

    private static final String PROFILE_STATUS = "ACTIVE";

    private final UserAccountService userAccountService;

    private final RoleService roleService;

    private final UserRoleService userRoleService;

    private final CitizenProfileService citizenProfileService;

    private final CitizenContactService citizenContactService;

    private final CitizenAddressService citizenAddressService;

    private final PasswordHashingService passwordHashingService;

    private final PiiEncryptionService piiEncryptionService;

    public CitizenRegistrationServiceImpl(
            UserAccountService userAccountService,
            RoleService roleService,
            UserRoleService userRoleService,
            CitizenProfileService citizenProfileService,
            CitizenContactService citizenContactService,
            CitizenAddressService citizenAddressService,
            PasswordHashingService passwordHashingService,
            PiiEncryptionService piiEncryptionService) {

        this.userAccountService = userAccountService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.citizenProfileService = citizenProfileService;
        this.citizenContactService = citizenContactService;
        this.citizenAddressService = citizenAddressService;
        this.passwordHashingService = passwordHashingService;
        this.piiEncryptionService = piiEncryptionService;
    }

    @Override
    public CitizenRegistrationResponse register(
            CitizenRegistrationRequest request) {

        validatePasswords(request);

        String passwordHash =
                passwordHashingService.hash(
                        request.password());

        CreateUserAccountRequest userRequest =
                new CreateUserAccountRequest(
                        request.email(),
                        request.username(),
                        ACCOUNT_STATUS);

        UserAccountResponse user =
                userAccountService.createUser(
                        userRequest,
                        passwordHash);

        RoleResponse citizenRole =
                roleService.getRoleByCode(
                        CITIZEN_ROLE);

        UserRoleResponse roleAssignment =
                userRoleService.assignRole(
                        user.userId(),
                        citizenRole.roleId());

        CitizenProfileResponse profile =
                citizenProfileService.createProfile(
                        user.userId(),
                        new CreateCitizenProfileRequest(
                                request.publicAlias(),
                                PROFILE_STATUS));

        citizenContactService.createContact(
                profile.citizenId(),
                new CreateCitizenContactRequest(
                        piiEncryptionService.encrypt(
                                request.phone()),
                        piiEncryptionService.encrypt(
                                request.email())));

        citizenAddressService.createAddress(
                profile.citizenId(),
                new CreateCitizenAddressRequest(
                        piiEncryptionService.encrypt(
                                request.address()),
                        request.cityId(),
                        request.postalCode(),
                        true));

        return new CitizenRegistrationResponse(
                profile.citizenId(),
                user.userId(),
                user.username(),
                profile.publicAlias(),
                profile.profileStatus(),
                user.status(),
                roleAssignment.roleCode());
    }

    private void validatePasswords(
            CitizenRegistrationRequest request) {

        if (!request.password().equals(
                request.confirmPassword())) {

            throw new InvalidRegistrationDataException();
        }
    }
}
