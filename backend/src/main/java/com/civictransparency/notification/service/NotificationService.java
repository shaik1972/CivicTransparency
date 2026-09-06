package com.civictransparency.notification.service;

import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.notification.entity.Notification;
import com.civictransparency.notification.entity.NotificationDelivery;
import com.civictransparency.notification.repository.NotificationDeliveryRepository;
import com.civictransparency.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationDeliveryRepository deliveryRepository) {
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Notify the citizen that their complaint was received and routed.
     */
    @Transactional
    public void notifyComplaintRouted(Complaint complaint, AuthorityDepartment department) {
        // Notification to citizen (identified via complaint; actual user lookup deferred)
        Notification citizenNotification = new Notification(
            UUID.randomUUID(),
            complaint.getCitizen().getCitizenId(), // using citizen ID as recipient key for now
            "COMPLAINT_ROUTED",
            "Your complaint has been received",
            "Your complaint '" + complaint.getComplaintReference() + "' has been assigned to " +
            department.getName() + ". We will keep you updated."
        );
        notificationRepository.save(citizenNotification);

        // Queue an IN_APP delivery record
        NotificationDelivery delivery = new NotificationDelivery(
            UUID.randomUUID(),
            citizenNotification,
            "IN_APP"
        );
        deliveryRepository.save(delivery);
    }

    /**
     * Notify the authority department of a new complaint assignment.
     */
    @Transactional
    public void notifyDepartmentAssigned(Complaint complaint, AuthorityDepartment department) {
        Notification deptNotification = new Notification(
            UUID.randomUUID(),
            department.getDepartmentId(),
            "COMPLAINT_ASSIGNED",
            "New complaint assigned to your department",
            "Complaint '" + complaint.getComplaintReference() + "' — " + complaint.getTitle() +
            " — has been routed to your department. Please review and take action."
        );
        notificationRepository.save(deptNotification);

        NotificationDelivery delivery = new NotificationDelivery(
            UUID.randomUUID(),
            deptNotification,
            "IN_APP"
        );
        deliveryRepository.save(delivery);
    }
}
