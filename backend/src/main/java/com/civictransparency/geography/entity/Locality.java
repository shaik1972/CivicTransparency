package com.civictransparency.geography.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "locality", schema = "geography")
public class Locality {

    @Id
    @Column(name = "locality_id", nullable = false)
    private UUID localityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected Locality() {
        // Required by JPA
    }

    public UUID getLocalityId() {
        return localityId;
    }

    public Ward getWard() {
        return ward;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
