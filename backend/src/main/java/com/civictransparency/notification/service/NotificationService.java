package com.civictransparency.notification.service;

import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.authority.entity.AuthorityUser;
import com.civictransparency.authority.repository.AuthorityUserRepository;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.notification.entity.Notification;
import com.civictransparency.notification.entity.NotificationDelivery;
import com.civictransparency.notification.repository.NotificationDeliveryRepository;
import com.civictransparency.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final AuthorityUserRepository authorityUserRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationDeliveryRepository deliveryRepository,
                               AuthorityUserRepository authorityUserRepository) {
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
        this.authorityUserRepository = authorityUserRepository;
    }

    @Transactional
    public void notifyComplaintRouted(Complaint complaint, AuthorityDepartment department) {
        UUID citizenUserId = complaint.getCitizen().getUser().getUserId();

        Notification citizenNotification = new Notification(
            UUID.randomUUID(),
            citizenUserId,
            "COMPLAINT_ROUTED",
            "Your complaint has been received",
            "Your complaint '" + complaint.getComplaintReference() + "' has been assigned to " +
            department.getName() + ". We will keep you updated."
        );
        notificationRepository.save(citizenNotification);

        deliveryRepository.save(new NotificationDelivery(
            UUID.randomUUID(),
            citizenNotification,
            "IN_APP"
        ));
    }

    @Transactional
    public void notifyDepartmentAssigned(Complaint complaint, AuthorityDepartment department) {
        List<AuthorityUser> officers = authorityUserRepository.findAllByDepartment_DepartmentId(department.getDepartmentId());

        if (officers.isEmpty()) {
            return;
        }

        for (AuthorityUser authorityUser : officers) {
            UUID recipientUserId = authorityUser.getUserAccount().getUserId();

            Notification deptNotification = new Notification(
                UUID.randomUUID(),
                recipientUserId,
                "COMPLAINT_ASSIGNED",
                "New complaint assigned to your department",
                "Complaint '" + complaint.getComplaintReference() + "' — " + complaint.getTitle() +
                " — has been routed to your department. Please review and take action."
            );
            notificationRepository.save(deptNotification);

            deliveryRepository.save(new NotificationDelivery(
                UUID.randomUUID(),
                deptNotification,
                "IN_APP"
            ));
        }
    }
}
