package com.example.shopapp.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CouponResultDTO {
    private double originalAmount;
    private double discount;
    private double finalAmount;
    private List<String> appliedConditions;
}
