package com.example.shopapp.dtos.responses;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse extends BaseResponse {
    private String name;
    private Float price;
    private String thumbnail;
    private String description;

    @JsonProperty("category_id")
    private Integer categoryId;
}
