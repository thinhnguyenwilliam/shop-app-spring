package com.example.shopapp.service.impl;

import com.example.shopapp.dtos.request.CategoryDTO;
import com.example.shopapp.models.Category;
import com.example.shopapp.repositories.CategoryRepository;
import com.example.shopapp.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public Category createCategory(CategoryDTO categoryDTO) {
        Category newCategory = Category.builder()
                .name(categoryDTO.getName())
                .build();

        categoryRepository.save(newCategory);
        return newCategory;
    }

    @Override
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found for id: " + id));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void updateCategory(Integer id, CategoryDTO categoryDTO) {
        Category existingCategory = getCategoryById(id);
        existingCategory.setName(categoryDTO.getName());

        categoryRepository.save(existingCategory);
    }

    public void deleteCategoryById(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found for id: " + id);
        }
        categoryRepository.deleteById(id);
    }

}
