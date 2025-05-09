package com.example.shopapp.controllers;

import com.example.shopapp.dtos.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController
{
    @GetMapping("")
    public ResponseEntity<String> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    )
    {
        return ResponseEntity.ok(String.format("Chao e iu hi getAllProducts page %d limit %d", page, limit));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<String> getProductById(@PathVariable("productId") String id)
    {
        return ResponseEntity.ok("Chao e iu hi getProductById " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        return ResponseEntity.ok("Chao e iu hi deleteProduct " + id);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body("Validation errors: " + String.join(", ", errors));
            }

            MultipartFile file = productDTO.getFile(); // ✅ Use a file from DTO

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is required.");
            }

            // Check file size (limit to 5MB)
            long maxFileSize = 5L * 1024 * 1024; // 5MB
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body("File size must be less than 5MB.");
            }

            // Check a file type
            String contentType = file.getContentType();
            if (!isImageTypeAllowed(contentType)) {
                return ResponseEntity.badRequest().body("Only PNG, JPG, and JPEG images are allowed.");
            }

            // Save file
            String savedFilename = saveFile(file);
            return ResponseEntity.ok("Product received with image saved as: " + savedFilename);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }


    // Helper method
    private boolean isImageTypeAllowed(String contentType) {
        return contentType != null && (
                contentType.equals("image/png") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/jpeg")
        );
    }

    private String saveFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads/";

        // Create a directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Get original filename
        String originalFilename = file.getOriginalFilename();

        // Ensure a filename is valid and contains an extension
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IOException("Invalid file name: no extension found.");
        }

        // Extract extension safely
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // Generate unique filename
        String newFilename = UUID.randomUUID().toString() + extension;

        // Save the file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return newFilename;
    }


}
