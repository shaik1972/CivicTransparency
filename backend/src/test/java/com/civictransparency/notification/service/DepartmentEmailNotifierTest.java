package com.civictransparency.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DepartmentEmailNotifierTest {

    @Test
    void sendsMinimalAssignmentNoticeToConfiguredDepartmentRecipient() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DepartmentEmailProperties properties = new DepartmentEmailProperties();
        properties.setFrom("civic-transparency@example.test");
        properties.setPortalUrl("http://localhost:3000/authority");
        properties.setDepartmentRecipients(Map.of("ROADS", "roads@example.test"));
        DepartmentEmailNotifier notifier = new DepartmentEmailNotifier(mailSender, properties);

        notifier.notifyAssigned("CMP-12345678", "roads", "Roads Department");

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("roads@example.test", message.getTo()[0]);
        assertEquals("civic-transparency@example.test", message.getFrom());
        assertEquals("Civic complaint assigned: CMP-12345678", message.getSubject());
        assertFalse(message.getText().contains("citizen"));
        assertFalse(message.getText().contains("description"));
        assertTrue(message.getText().contains("http://localhost:3000/authority"));
    }

    @Test
    void doesNotSendWhenDepartmentHasNoConfiguredRecipient() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DepartmentEmailProperties properties = new DepartmentEmailProperties();
        properties.setFrom("civic-transparency@example.test");
        properties.setDepartmentRecipients(Map.of("ROADS", "roads@example.test"));
        DepartmentEmailNotifier notifier = new DepartmentEmailNotifier(mailSender, properties);

        notifier.notifyAssigned("CMP-12345678", "PARKS", "Parks Department");

        verifyNoInteractions(mailSender);
    }
}
