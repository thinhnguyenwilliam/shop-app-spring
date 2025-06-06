package com.example.shopapp.controllers;

import com.example.shopapp.dtos.responses.HealthCheckResponse;
import com.example.shopapp.models.Category;
import com.example.shopapp.service.ICategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/healthcheck")
@AllArgsConstructor
public class HealthCheckController {
    private final ICategoryService categoryService;

    @GetMapping("/health")
    public ResponseEntity<Object> healthCheck() {
        try {
            //List<Category> categories = categoryService.getAllCategories();
            //HealthCheckResponse response = new HealthCheckResponse("ok", categories);
            //return ResponseEntity.ok(response);
            String computerName = InetAddress.getLocalHost().getHostName();
            return ResponseEntity.ok(Map.of("computerName", computerName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HealthCheckResponse("failed", null));
        }
    }

}