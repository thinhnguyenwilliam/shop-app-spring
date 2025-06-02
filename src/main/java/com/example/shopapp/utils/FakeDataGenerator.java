package com.example.shopapp.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import com.example.shopapp.models.Product;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.CategoryRepository;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FakeDataGenerator {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    //@PostConstruct
    public void generateFakeProducts() {
        int categoryId = 3; // Ensure this category exists

        for (int i = 0; i < 20; i++) {
            Product product = new Product();
            product.setName(faker.commerce().productName());
            product.setSlug("product-" + UUID.randomUUID());

           // product.setPrice(BigDecimal.valueOf(Double.parseDouble(faker.commerce().price())));
            product.setPrice(Float.parseFloat(faker.commerce().price()));

            product.setDescription(faker.lorem().sentence(10));
            //product.setThumbnail("https://via.placeholder.com/150");
            product.setCategory(categoryRepository.findById(categoryId).orElse(null));

            // Manually set createdAt and updatedAt
            LocalDateTime randomDate = faker.date().past(100, java.util.concurrent.TimeUnit.DAYS).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

            product.setCreatedAt(randomDate);
            product.setUpdatedAt(randomDate.plusDays(random.nextInt(10)));

            productRepository.save(product);
        }

        System.out.println("✅ Fake products inserted");
    }
}
