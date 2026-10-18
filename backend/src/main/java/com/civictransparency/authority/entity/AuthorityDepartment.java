package com.civictransparency.authority.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "authority_department", schema = "authority")
public class AuthorityDepartment {

    @Id
    @Column(name = "department_id")
    private UUID departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id", nullable = false)
    private Authority authority;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    protected AuthorityDepartment() {}

    public AuthorityDepartment(UUID departmentId, Authority authority, String code, String name, String status) {
        this.departmentId = departmentId;
        this.authority = authority;
        this.code = code;
        this.name = name;
        this.status = status;
    }

    public UUID getDepartmentId() { return departmentId; }
    public Authority getAuthority() { return authority; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
