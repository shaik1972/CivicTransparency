package com.civictransparency.notification.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "civic.notifications.email")
public class DepartmentEmailProperties {

    private boolean enabled;
    private String from;
    private String portalUrl = "http://localhost:3000/authority";
    private Map<String, String> departmentRecipients = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPortalUrl() {
        return portalUrl;
    }

    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public Map<String, String> getDepartmentRecipients() {
        return departmentRecipients;
    }

    public void setDepartmentRecipients(Map<String, String> departmentRecipients) {
        this.departmentRecipients = departmentRecipients;
    }
}
