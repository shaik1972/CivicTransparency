package com.civictransparency.citizen.entity;

import com.civictransparency.identity.entity.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "citizen_profile", schema = "citizen")
public class CitizenProfile {

    @Id
    @Column(name = "citizen_id", nullable = false)
    private UUID citizenId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Column(name = "public_alias", length = 100)
    private String publicAlias;

    @Column(name = "profile_status", nullable = false, length = 30)
    private String profileStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CitizenProfile() {
        // Required by JPA
    }

    public CitizenProfile(UserAccount user, String profileStatus) {
        this.citizenId = UUID.randomUUID();
        this.user = user;
        this.profileStatus = profileStatus;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public CitizenProfile(UserAccount user, String publicAlias, String profileStatus) {
        this(user, profileStatus);
        this.publicAlias = publicAlias;
    }

    public UUID getCitizenId() {
        return citizenId;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getPublicAlias() {
        return publicAlias;
    }

    public void setPublicAlias(String publicAlias) {
        this.publicAlias = publicAlias;
    }

    public String getProfileStatus() {
        return profileStatus;
    }

    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}