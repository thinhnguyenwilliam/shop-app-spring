package com.example.shopapp.service;

import com.example.shopapp.models.Category;
import com.example.shopapp.dtos.CategoryDTO;
import java.util.List;

public interface ICategoryService
{
    void createCategory(CategoryDTO categoryDTO);
    Category getCategoryById(Integer id);
    List<Category> getAllCategories();
    Category updateCategory(Integer id, CategoryDTO categoryDTO);
    void deleteCategoryById(Integer id);
}
