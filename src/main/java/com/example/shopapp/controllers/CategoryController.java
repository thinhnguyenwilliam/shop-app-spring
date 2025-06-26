package com.example.shopapp.controllers;


import com.example.shopapp.components.CategoryMessageConverter;
import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.dtos.request.CategoryDTO;
import com.example.shopapp.dtos.responses.ResponseObject;
import com.example.shopapp.models.Category;
import com.example.shopapp.service.impl.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController
{
    private final CategoryService categoryService;
    private final LocalizationUtils localizationUtils;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getCategoryById(
            @PathVariable("id") Integer categoryId
    ) {
        Category existingCategory = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ResponseObject.builder()
                .data(existingCategory)
                .message("Get category information successfully")
                .status(HttpStatus.OK)
                .build());
    }

    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    )
    {
        List<Category> categories= categoryService.getAllCategories();
        kafkaTemplate.send("get-all-categories", categories);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("")
    public ResponseEntity<String> createCategory(
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
        Category newCategory = categoryService.createCategory(categoryDTO);
        kafkaTemplate.send("insert-a-category", newCategory);//producer
        kafkaTemplate.setMessageConverter(new CategoryMessageConverter());

        return ResponseEntity.ok("Chao e iu hi, received category: " + categoryDTO.getName());
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryDTO categoryDTO
    )
    {
        categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok("Chao e iu hi updateCategory");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok("Chao e iu hi deleteCategory " + id);
    }
}
