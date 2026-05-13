package com.javaweb.service.impl;

import com.javaweb.dto.CategoryDTO;
import com.javaweb.entity.Category;
import com.javaweb.repository.CategoryRepository;
import com.javaweb.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = new ArrayList<>();

        for (Category category : categories) {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setSlug(category.getSlug());
            dto.setStatus(category.getStatus());
            dto.setRating(category.getRating());
            if (category.getParentCategory() != null) {
                dto.setParentId(category.getParentCategory().getId());
                dto.setParentName(category.getParentCategory().getName());
            }
            categoryDTOs.add(dto);
        }
        return categoryDTOs;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setSlug(categoryDTO.getSlug());
        category.setStatus(categoryDTO.getStatus());
        category.setRating(categoryDTO.getRating());
        
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent Category not found"));
            category.setParentCategory(parent);
        }
        
        category = categoryRepository.save(category);
        categoryDTO.setId(category.getId());
        return categoryDTO;
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setName(categoryDTO.getName());
        category.setSlug(categoryDTO.getSlug());
        category.setStatus(categoryDTO.getStatus());
        category.setRating(categoryDTO.getRating());
        
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent Category not found"));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }
        
        categoryRepository.save(category);
        categoryDTO.setId(category.getId());
        return categoryDTO;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }
}
