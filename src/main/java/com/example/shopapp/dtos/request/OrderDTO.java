package com.example.shopapp.dtos.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO
{
    @JsonProperty("user_id")
    @Min(value = 1, message = "User id must be greater than or equal to one")
    Integer userId;

    @JsonProperty("fullname")
    String fullName;

    String email;

    @JsonProperty("phone_number")
    @NotBlank(message = "Phone number must not be blank")
    @Size(min = 5, max = 10, message = "Phone number must be 10 digits")
    String phoneNumber;

    String address;
    String note;

    @JsonProperty("total_money")
    @Min(value = 0, message = "Total money must be greater than or equal to zero")
    Float totalMoney;

    @JsonProperty("shipping_method")
    String shippingMethod;

    @JsonProperty("shipping_address")
    String shippingAddress;

    @JsonProperty("payment_method")
    String paymentMethod;

    @JsonProperty("shipping_date")
    Date shippingDate;
}
