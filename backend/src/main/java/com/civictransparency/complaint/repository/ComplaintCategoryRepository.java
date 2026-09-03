package com.civictransparency.complaint.repository;

import com.civictransparency.complaint.entity.ComplaintCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComplaintCategoryRepository extends JpaRepository<ComplaintCategory, UUID> {

    List<ComplaintCategory> findByActiveTrueOrderByDisplayOrderAsc();
}
