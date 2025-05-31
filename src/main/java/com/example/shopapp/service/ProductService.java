package com.example.shopapp.service;

import com.example.shopapp.dtos.request.ProductDTO;
import com.example.shopapp.dtos.request.ProductImageDTO;
import com.example.shopapp.dtos.responses.ProductResponse;
import com.example.shopapp.exceptions.InvalidParamException;
import com.example.shopapp.models.Category;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.repositories.CategoryRepository;
import com.example.shopapp.repositories.ProductImageRepository;
import com.example.shopapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService
{
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) {
        // Step 1: Find the category
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + productDTO.getCategoryId()));


        // Step 2: Create a Product entity
        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail()) // Assuming this is handled by file upload elsewhere
                .description(productDTO.getDescription())
                .category(category)
                .slug(productDTO.getSlug() != null ? productDTO.getSlug() : productDTO.getName().toLowerCase().replace(" ", "-"))
                .build();

        // Step 3: Save a product
        return productRepository.save(product);
    }


    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));
    }

    @Override
    public Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) {
        keyword = keyword == null ? "" : keyword.trim().toLowerCase();
        Page<Product> productPage = productRepository.findByKeywordAndCategory(keyword, categoryId, pageRequest);
        return productPage.map(ProductResponse::fromProduct);
    }



    @Override
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductDTO productDTO) {
        Product existingProduct = getProductById(id);
        if (existingProduct != null) {
            Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + productDTO.getCategoryId()));

            existingProduct.setName(productDTO.getName());
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setThumbnail(productDTO.getThumbnail());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setCategory(existingCategory);

            Product savedProduct = productRepository.save(existingProduct);
            return ProductResponse.fromProduct(savedProduct);
        }

        throw new IllegalArgumentException("Product not found with ID: " + id);
    }


    @Override
    public void deleteProductById(Integer id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
    }


    @Override
    public Boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(Integer productId, ProductImageDTO productImageDTO)
            throws InvalidParamException
    {
        // Ensure the product exists
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productId));

        // Check the current number of images for the product
        int size = productImageRepository.findAllByProductId(productId).size();
        if (size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new InvalidParamException("Product can have a maximum of 5 images");
        }

        // Create and save a new product image
        ProductImage newProductImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();

        return productImageRepository.save(newProductImage);
    }

    @Override
    public List<Product> findProductsByIds(List<Integer> productIds) {
        //return productRepository.findProductsByIds(productIds); // cach 1
        return productRepository.findAllById(productIds); // cach 2
    }



}
