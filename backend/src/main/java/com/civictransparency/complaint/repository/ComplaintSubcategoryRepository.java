package com.civictransparency.complaint.repository;

import com.civictransparency.complaint.entity.ComplaintSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComplaintSubcategoryRepository extends JpaRepository<ComplaintSubcategory, UUID> {

    List<ComplaintSubcategory> findByCategoryCategoryIdAndActiveTrueOrderByNameAsc(UUID categoryId);
}
