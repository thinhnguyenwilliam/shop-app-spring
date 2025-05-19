package com.example.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO
{
    @NotBlank(message = "Product name must not be blank")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Min(value = 0, message = "Product price must be greater than or equal to zero")
    @Max(value = 1000000, message = "Product price must be less than or equal to 1000000")
    private Float price;


    private String thumbnail;
    private String description;

    private String slug;

    @JsonProperty("category_id")
    private Integer categoryId;

}
