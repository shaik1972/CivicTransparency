package com.civictransparency.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "role", schema = "identity")
public class Role {

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    protected Role() {
        // Required by JPA
    }

    public Role(String code, String name) {
        this.roleId = UUID.randomUUID();
        this.code = code;
        this.name = name;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}