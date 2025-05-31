package com.example.shopapp.controllers;

import com.example.shopapp.dtos.request.ProductDTO;
import com.example.shopapp.dtos.request.ProductImageDTO;
import com.example.shopapp.dtos.responses.ProductListResponse;
import com.example.shopapp.dtos.responses.ProductResponse;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController
{
    private final IProductService productService;

    @GetMapping("/multiple")
    public ResponseEntity<Object> getMultipleProducts(@RequestParam("ids") String ids) {
        try {
            // Convert comma-separated string into List<Integer>
            List<Integer> productIds = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList(); // Java 16+

            List<Product> products = productService.findProductsByIds(productIds);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch products.");
        }
    }



    @GetMapping("/images/{imageName}")
    public ResponseEntity<Object> viewImage(@PathVariable String imageName) {
        try {
            Path imagePath = Paths.get("uploads", imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            //log.info("Image path: {}", imagePath);
            //log.info("File exists: {}", Files.exists(imagePath));
            //log.info("Readable: {}", Files.isReadable(imagePath));
            //log.info("Working directory: {}", System.getProperty("user.dir"));

            if (!resource.exists() || !resource.isReadable()) {
                // Fallback to default 404 image
                imagePath = Paths.get("uploads", "404-notfound.png");
                resource = new UrlResource(imagePath.toUri());
            }

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(imagePath);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException ex) {
            log.error("Error reading image file", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not read the file.");
        }
    }

    @GetMapping("")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(value = "keyword", defaultValue = "") String keyword, // use to search by keyword
            @RequestParam(value = "category_id", defaultValue = "0") Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<ProductResponse> products = productService.getAllProducts(keyword, categoryId, pageRequest);

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
    public ResponseEntity<Object> getProductById(@PathVariable("productId") Integer id)
    {
        Product existingProduct= productService.getProductById(id);
        return ResponseEntity.ok(ProductResponse.fromProduct(existingProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok("Chao e iu hi deleteProduct " + id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductDTO productDTO
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
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
