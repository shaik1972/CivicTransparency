package com.civictransparency.authority.entity;

import com.civictransparency.identity.entity.UserAccount;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "authority_user", schema = "authority")
public class AuthorityUser {

    @Id
    @Column(name = "authority_user_id")
    private UUID authorityUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private AuthorityDepartment department;

    @Column(name = "employee_reference", length = 100)
    private String employeeReference;

    protected AuthorityUser() {}

    public AuthorityUser(UUID authorityUserId, UserAccount userAccount, AuthorityDepartment department, String employeeReference) {
        this.authorityUserId = authorityUserId;
        this.userAccount = userAccount;
        this.department = department;
        this.employeeReference = employeeReference;
    }

    public UUID getAuthorityUserId() { return authorityUserId; }
    public UserAccount getUserAccount() { return userAccount; }
    public AuthorityDepartment getDepartment() { return department; }
    public String getEmployeeReference() { return employeeReference; }
}
