package com.example.shopapp.service;

import com.example.shopapp.models.Category;
import com.example.shopapp.dtos.request.CategoryDTO;
import java.util.List;

public interface ICategoryService
{
    Category createCategory(CategoryDTO categoryDTO);
    Category getCategoryById(Integer id);
    List<Category> getAllCategories();
    void updateCategory(Integer id, CategoryDTO categoryDTO);
    void deleteCategoryById(Integer id);
}
