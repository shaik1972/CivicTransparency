package com.civictransparency.authority.entity;

import com.civictransparency.geography.entity.City;
import com.civictransparency.geography.entity.State;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "authority", schema = "authority")
public class Authority {

    @Id
    @Column(name = "authority_id")
    private UUID authorityId;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    protected Authority() {}

    public Authority(UUID authorityId, String code, String name, State state, City city, String status) {
        this.authorityId = authorityId;
        this.code = code;
        this.name = name;
        this.state = state;
        this.city = city;
        this.status = status;
    }

    public UUID getAuthorityId() { return authorityId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public State getState() { return state; }
    public City getCity() { return city; }
    public String getStatus() { return status; }
}
