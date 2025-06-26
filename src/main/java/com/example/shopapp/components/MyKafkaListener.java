package com.example.shopapp.components;

import com.example.shopapp.models.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@KafkaListener(
        id = "groupA",
        topics = { "get-all-categories", "insert-a-category" },
        groupId = "category-group",
        containerFactory = "kafkaListenerContainerFactory"
)
public class MyKafkaListener {

    @KafkaHandler
    public void listenCategory(Category category) {
        log.info("✅ Received single category: {}", category);
    }

    @KafkaHandler
    public void listenListOfCategories(List<Category> categories) {
        log.info("✅ Received list of categories ({}): {}", categories.size(), categories);
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) {
        log.warn("⚠️ Received unknown message type: {}", object);
    }
}
