package com.civictransparency.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "civic.notifications.email", name = "enabled", havingValue = "true")
public class DepartmentEmailNotifier {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentEmailNotifier.class);

    private final JavaMailSender mailSender;
    private final DepartmentEmailProperties properties;

    public DepartmentEmailNotifier(JavaMailSender mailSender, DepartmentEmailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void notifyAssigned(String complaintReference, String departmentCode, String departmentName) {
        String recipient = getRecipient(departmentCode);
        if (recipient == null || recipient.isBlank()) {
            logger.warn("Email notification is enabled, but no recipient is configured for department {}",
                    departmentCode);
            return;
        }
        if (properties.getFrom() == null || properties.getFrom().isBlank()) {
            logger.error("Email notification for department {} was skipped because civic.notifications.email.from is empty",
                    departmentCode);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(recipient);
        message.setSubject("Civic complaint assigned: " + complaintReference);
        message.setText("""
                A civic complaint has been assigned to %s.

                Complaint reference: %s
                Sign in to the authority portal to review the complaint and update its status:
                %s
                """.formatted(departmentName, complaintReference, properties.getPortalUrl()));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            logger.error("Email notification failed for complaint {} and department {}",
                    complaintReference, departmentCode, ex);
        }
    }

    private String getRecipient(String departmentCode) {
        return properties.getDepartmentRecipients().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(departmentCode))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
