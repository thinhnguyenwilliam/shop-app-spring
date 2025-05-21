package com.example.shopapp.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailDTO {
    @JsonProperty("order_id")
    @Min(value=1, message = "Order id must be greater than or equal to one")
    Integer orderId;

    @JsonProperty("product_id")
    @Min(value=1, message = "Product id must be greater than or equal to one")
    Integer productId;

    @Min(value=0, message = "Price must be greater than or equal to zero")
    Float price;

    @JsonProperty("number_of_products")
    @Min(value=1, message = "Number of products must be greater than or equal to one")
    Integer numberOfProducts;

    @JsonProperty("total_money")
    @Min(value=0, message = "Total money must be greater than or equal to zero")
    Float totalMoney;

    String color;
}
