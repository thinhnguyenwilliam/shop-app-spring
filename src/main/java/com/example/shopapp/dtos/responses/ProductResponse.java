package com.example.shopapp.dtos.responses;


import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse extends BaseResponse {
    private Integer id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;

    @JsonProperty("category_id")
    private Integer categoryId;

    @JsonProperty("product_images")
    private List<ProductImage> productImages = new ArrayList<>();

    public static ProductResponse fromProduct(Product product){
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .productImages(product.getProductImages())
                .build();
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }
}
