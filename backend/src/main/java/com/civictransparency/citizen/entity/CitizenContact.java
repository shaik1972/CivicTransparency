package com.civictransparency.citizen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "citizen_contact", schema = "citizen")
public class CitizenContact {

    @Id
    @Column(name = "citizen_id", nullable = false)
    private java.util.UUID citizenId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "citizen_id", nullable = false)
    private CitizenProfile citizenProfile;

    @Column(name = "phone_encrypted", nullable = false)
    private String phoneEncrypted;

    @Column(name = "email_encrypted", nullable = false)
    private String emailEncrypted;

    @Column(name = "verified_phone", nullable = false)
    private boolean verifiedPhone;

    @Column(name = "verified_email", nullable = false)
    private boolean verifiedEmail;

    protected CitizenContact() {
        // Required by JPA
    }

    public CitizenContact(
            CitizenProfile citizenProfile,
            String phoneEncrypted,
            String emailEncrypted) {

        this.citizenProfile = citizenProfile;
        this.citizenId = citizenProfile.getCitizenId();
        this.phoneEncrypted = phoneEncrypted;
        this.emailEncrypted = emailEncrypted;
        this.verifiedPhone = false;
        this.verifiedEmail = false;
    }

    public java.util.UUID getCitizenId() {
        return citizenId;
    }

    public CitizenProfile getCitizenProfile() {
        return citizenProfile;
    }

    public String getPhoneEncrypted() {
        return phoneEncrypted;
    }

    public void setPhoneEncrypted(String phoneEncrypted) {
        this.phoneEncrypted = phoneEncrypted;
    }

    public String getEmailEncrypted() {
        return emailEncrypted;
    }

    public void setEmailEncrypted(String emailEncrypted) {
        this.emailEncrypted = emailEncrypted;
    }

    public boolean isVerifiedPhone() {
        return verifiedPhone;
    }

    public void setVerifiedPhone(boolean verifiedPhone) {
        this.verifiedPhone = verifiedPhone;
    }

    public boolean isVerifiedEmail() {
        return verifiedEmail;
    }

    public void setVerifiedEmail(boolean verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }
}