package com.example.shopapp.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private String message;
    private int page;
    private int limit;
    private int totalPages;
    private long totalItems;
    private List<ProductResponse> products;
}
