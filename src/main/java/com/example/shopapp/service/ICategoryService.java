package com.example.shopapp.service;

import com.example.shopapp.models.Category;

import java.util.List;

public interface ICategoryService
{
    Category createCategory(Category category);
    Category getCategoryById(Integer id);
    List<Category> getAllCategories();
    Category updateCategory(Integer id, Category category);
    void deleteCategoryById(Integer id);
}
