package com.example.shopapp.dtos.responses;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthCheckResponse {
    private String status;
    private Object data;
}
