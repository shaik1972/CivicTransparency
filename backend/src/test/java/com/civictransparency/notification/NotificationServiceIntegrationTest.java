package com.civictransparency.notification;

import com.civictransparency.authority.entity.Authority;
import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.authority.entity.AuthorityUser;
import com.civictransparency.authority.repository.AuthorityDepartmentRepository;
import com.civictransparency.authority.repository.AuthorityRepository;
import com.civictransparency.authority.repository.AuthorityUserRepository;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.complaint.entity.Severity;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.notification.entity.Notification;
import com.civictransparency.notification.repository.NotificationRepository;
import com.civictransparency.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AuthorityDepartmentRepository authorityDepartmentRepository;

    @Autowired
    private AuthorityUserRepository authorityUserRepository;

    @Test
    void shouldSendNotificationsToActualCitizenAndAuthorityUserRecipients() {
        UserAccount citizenUser = new UserAccount("citizen.notify@example.com", "ACTIVE");
        citizenUser.setUsername("citizen-notify");
        citizenUser = userAccountRepository.save(citizenUser);

        CitizenProfile citizenProfile = new CitizenProfile(citizenUser, "Citizen Notify", "ACTIVE");

        Complaint complaint = new Complaint(
                UUID.randomUUID(),
                "CT-1001",
                citizenProfile,
                null,
                null,
                "Broken road",
                "Large pothole near the school gate",
                Severity.MEDIUM,
                null,
                null
        );

        Authority authority = new Authority(UUID.randomUUID(), "PWD", "Public Works Department", null, null, "ACTIVE");
        authority = authorityRepository.save(authority);

        AuthorityDepartment department = new AuthorityDepartment(UUID.randomUUID(), authority, "ROAD", "Road Maintenance", "ACTIVE");
        department = authorityDepartmentRepository.save(department);

        UserAccount officerUser = new UserAccount("officer.notify@example.com", "ACTIVE");
        officerUser.setUsername("officer-notify");
        officerUser = userAccountRepository.save(officerUser);

        AuthorityUser authorityUser = new AuthorityUser(UUID.randomUUID(), officerUser, department, "EMP-100");
        authorityUserRepository.save(authorityUser);

        notificationService.notifyComplaintRouted(complaint, department);
        notificationService.notifyDepartmentAssigned(complaint, department);

        List<Notification> notifications = notificationRepository.findAll();
        final UUID expectedCitizenUserId = citizenUser.getUserId();
        final UUID expectedOfficerUserId = officerUser.getUserId();

        assertThat(notifications)
                .anySatisfy(n -> {
                    assertThat(n.getCategory()).isEqualTo("COMPLAINT_ROUTED");
                    assertThat(n.getRecipientUserId()).isEqualTo(expectedCitizenUserId);
                });

        assertThat(notifications)
                .anySatisfy(n -> {
                    assertThat(n.getCategory()).isEqualTo("COMPLAINT_ASSIGNED");
                    assertThat(n.getRecipientUserId()).isEqualTo(expectedOfficerUserId);
                });
    }
}
