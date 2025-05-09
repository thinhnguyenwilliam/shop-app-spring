package com.example.shopapp.controllers;


import com.example.shopapp.dtos.CategoryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/categories")
//@Validated
public class CategoryController
{
    @GetMapping("")
    public ResponseEntity<String> getAllCategories(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    )
    {
        return ResponseEntity.ok(String.format("Chao e iu hi getAllCategories page %d limit %d", page, limit));
    }

    @PostMapping("")
    public ResponseEntity<String> insertCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .toList();

            return ResponseEntity.badRequest().body("Validation errors: " + String.join(", ", errors));
        }

        return ResponseEntity.ok("Chao e iu hi, received category: " + categoryDTO.getName());
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
