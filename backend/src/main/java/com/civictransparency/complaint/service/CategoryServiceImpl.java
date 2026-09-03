package com.civictransparency.complaint.service;

import com.civictransparency.complaint.dto.CategoryResponse;
import com.civictransparency.complaint.dto.SubcategoryResponse;
import com.civictransparency.complaint.entity.ComplaintCategory;
import com.civictransparency.complaint.entity.ComplaintSubcategory;
import com.civictransparency.complaint.repository.ComplaintCategoryRepository;
import com.civictransparency.complaint.repository.ComplaintSubcategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final ComplaintCategoryRepository categoryRepository;
    private final ComplaintSubcategoryRepository subcategoryRepository;

    public CategoryServiceImpl(
            ComplaintCategoryRepository categoryRepository,
            ComplaintSubcategoryRepository subcategoryRepository) {

        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {

        return categoryRepository
                .findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(category -> {
                    List<SubcategoryResponse> subcategories = subcategoryRepository
                            .findByCategoryCategoryIdAndActiveTrueOrderByNameAsc(category.getCategoryId())
                            .stream()
                            .map(sub -> toSubcategoryResponse(sub, category))
                            .toList();
                    return toCategoryResponse(category, subcategories);
                })
                .toList();
    }

    private CategoryResponse toCategoryResponse(
            ComplaintCategory category,
            List<SubcategoryResponse> subcategories) {

        return new CategoryResponse(
                category.getCategoryId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getIconName(),
                category.getDisplayOrder(),
                subcategories);
    }

    private SubcategoryResponse toSubcategoryResponse(
            ComplaintSubcategory sub,
            ComplaintCategory category) {

        return new SubcategoryResponse(
                sub.getSubcategoryId(),
                category.getCategoryId(),
                sub.getCode(),
                sub.getName(),
                sub.getDescription(),
                sub.getDefaultPriority());
    }
}
