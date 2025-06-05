package com.example.shopapp.service;

import com.example.shopapp.dtos.responses.ProductResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductRedisService implements IProductRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;
    private static final String PRODUCT_KEY_PREFIX = "all_products";

    private String getKeyFrom(String keyword,
                              Long categoryId,
                              PageRequest pageRequest) {

        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();
        Sort.Order order = pageRequest.getSort().getOrderFor("id");
        String sortDirection = (order != null && order.getDirection() == Sort.Direction.ASC) ? "asc" : "desc";

        return String.format("%s:%s:%s:%d:%d:%s",
                PRODUCT_KEY_PREFIX,
                keyword == null ? "" : keyword.trim().toLowerCase(),
                categoryId == null ? "all" : categoryId,
                pageNumber,
                pageSize,
                sortDirection
        );
    }

    @Override
    public List<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId,
                                                PageRequest pageRequest) throws JsonProcessingException {

        String key = getKeyFrom(keyword, categoryId, pageRequest);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached == null) return null;

        String json = cached.toString();
        return redisObjectMapper.readValue(json, new TypeReference<List<ProductResponse>>() {});
    }

    @Override
    public void saveAllProducts(List<ProductResponse> productResponses,
                                String keyword,
                                Long categoryId,
                                PageRequest pageRequest) throws JsonProcessingException {

        String key = getKeyFrom(keyword, categoryId, pageRequest);
        String json = redisObjectMapper.writeValueAsString(productResponses);
        redisTemplate.opsForValue().set(key, json, 10, TimeUnit.MINUTES); // Cache for 10 minutes
    }

    @Override
    public void clear() {
        Set<String> keys = redisTemplate.keys(PRODUCT_KEY_PREFIX + ":*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}


