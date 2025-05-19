package com.example.shopapp.controllers;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.dtos.responses.ProductListResponse;
import com.example.shopapp.dtos.responses.ProductResponse;
import com.example.shopapp.exceptions.InvalidParamException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController
{
    private final IProductService productService;


    @GetMapping("")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        Page<ProductResponse> products = productService.getAllProducts(pageRequest);

        ProductListResponse response = ProductListResponse.builder()
                .message("Success")
                .page(page)
                .limit(limit)
                .totalPages(products.getTotalPages())
                .totalItems(products.getTotalElements())
                .products(products.getContent())
                .build();

        return ResponseEntity.ok(response);
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

    // === 1. Create product ===
    @PostMapping()
    public ResponseEntity<Object> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body(errors);
            }

            Product newProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    // === 2. Upload product images ===
    @PostMapping(value = "/uploads/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadImages(
            @PathVariable("productId") Integer productId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        try {
            files = (files != null) ? files : new ArrayList<>();
            List<ProductImage> productImages = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty() || file.getSize() == 0) {
                    continue;
                }

                long maxFileSize = 5L * 1024 * 1024; // 5MB
                if (file.getSize() > maxFileSize) {
                    return ResponseEntity.badRequest().body("File size must be less than 5MB.");
                }

                String contentType = file.getContentType();
                if (!isImageTypeAllowed(contentType)) {
                    return ResponseEntity.badRequest().body("Only PNG, JPG, and JPEG images are allowed.");
                }

                // Save file
                String savedFilename = saveFile(file);

                // Save image entry to DB
                ProductImage savedImage = productService.createProductImage(
                        productId,
                        ProductImageDTO.builder()
                                .productId(productId)
                                .imageUrl(savedFilename)
                                .build()
                );

                productImages.add(savedImage);
            }

            return ResponseEntity.ok(productImages);

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
