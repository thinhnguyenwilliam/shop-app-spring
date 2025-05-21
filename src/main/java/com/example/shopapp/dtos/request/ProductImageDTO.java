package com.example.shopapp.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageDTO
{
    @JsonProperty("product_id")
    @Min(value = 1, message = "Product id must be greater than or equal to one")
    private Integer productId;

    @JsonProperty("image_url")
    @Size(min = 1, max = 255, message = "Image url must be between 1 and 255 characters")
    private String imageUrl;
}
