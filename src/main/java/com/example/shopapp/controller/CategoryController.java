package com.example.shopapp.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController
{
    @GetMapping("")
    public ResponseEntity<String> getAllCategories()
    {
        return ResponseEntity.ok("Chao e iu");
    }

    @PostMapping("")
    public ResponseEntity<String> insertCategory()
    {
        return ResponseEntity.ok("Chao e iu hi ");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Chao e iu hi updateCategory");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Chao e iu hi deleteCategory " + id);
    }
}
