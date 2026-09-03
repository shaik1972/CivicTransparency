package com.civictransparency.geography.repository;

import com.civictransparency.geography.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StateRepository
        extends JpaRepository<State, UUID> {

    List<State> findByActiveTrueOrderByNameAsc();
}
