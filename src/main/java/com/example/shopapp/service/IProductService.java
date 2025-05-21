package com.example.shopapp.service;

import com.example.shopapp.dtos.request.ProductDTO;
import com.example.shopapp.dtos.request.ProductImageDTO;
import com.example.shopapp.dtos.responses.ProductResponse;
import com.example.shopapp.exceptions.InvalidParamException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IProductService
{
    Product createProduct(ProductDTO productDTO);
    Product getProductById(Integer id);

    Page<ProductResponse> getAllProducts(PageRequest pageRequest);
    ProductResponse updateProduct(Integer id, ProductDTO productDTO);
    void deleteProductById(Integer id);
    Boolean existsByName(String name);

    ProductImage createProductImage(Integer productId, ProductImageDTO productImageDTO)
            throws InvalidParamException;
}
