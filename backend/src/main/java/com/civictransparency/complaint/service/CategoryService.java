package com.civictransparency.complaint.service;

import com.civictransparency.complaint.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getActiveCategories();
}
