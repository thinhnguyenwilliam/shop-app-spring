package com.example.shopapp.service;

import com.example.shopapp.service.impl.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisService redisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisService = new RedisService(redisTemplate);
    }

    @Test
    void testSave() {
        String key = "testKey";
        String value = "testValue";

        redisService.save(key, value);

        verify(valueOperations).set(key, value);
    }

    @Test
    void testGet() {
        String key = "testKey";
        String expectedValue = "testValue";

        when(valueOperations.get(key)).thenReturn(expectedValue);

        Object actualValue = redisService.get(key);

        verify(valueOperations).get(key);
        assert actualValue.equals(expectedValue);
    }
}
