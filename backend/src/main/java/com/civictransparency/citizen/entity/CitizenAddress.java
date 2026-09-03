package com.civictransparency.citizen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "citizen_address", schema = "citizen")
public class CitizenAddress {

    @Id
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private CitizenProfile citizenProfile;

    @Column(name = "address_text_encrypted", nullable = false)
    private String addressTextEncrypted;

    @Column(name = "city_id")
    private UUID cityId;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    protected CitizenAddress() {
        // Required by JPA
    }

    public CitizenAddress(
            CitizenProfile citizenProfile,
            String addressTextEncrypted,
            UUID cityId,
            String postalCode,
            boolean primary) {

        this.addressId = UUID.randomUUID();
        this.citizenProfile = citizenProfile;
        this.addressTextEncrypted = addressTextEncrypted;
        this.cityId = cityId;
        this.postalCode = postalCode;
        this.primary = primary;
    }

    public UUID getAddressId() {
        return addressId;
    }

    public CitizenProfile getCitizenProfile() {
        return citizenProfile;
    }

    public String getAddressTextEncrypted() {
        return addressTextEncrypted;
    }

    public void setAddressTextEncrypted(String addressTextEncrypted) {
        this.addressTextEncrypted = addressTextEncrypted;
    }

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}