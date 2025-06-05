package com.example.shopapp.job;

import com.example.shopapp.models.Product;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.service.IProductRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ProductSyncJob {

    private final ProductRepository productRepository;
    private final IProductRedisService productRedisService;

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void syncIfUpdated() {
        // Optional: check for products updated in the last minute
        LocalDateTime since = LocalDateTime.now().minusMinutes(1);

        List<Product> updatedProducts = productRepository.findByUpdatedAtAfter(since);
        if (!updatedProducts.isEmpty()) {
            productRedisService.clear();
            System.out.println("Redis cache cleared due to external DB updates.");
        }
    }
}

