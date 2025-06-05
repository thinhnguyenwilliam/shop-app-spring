package com.example.shopapp.controllers;

import com.example.shopapp.service.RedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/redis")
public class RedisController {

    private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/save")
    public ResponseEntity<Object> save(@RequestParam String key, @RequestParam String value) {
        redisService.save(key, value);
        return ResponseEntity.ok().body(Map.of("message", "Saved", "key", key, "value", value));
    }

    @GetMapping("/get")
    public ResponseEntity<Object> get(@RequestParam String key) {
        Object value = redisService.get(key);
        return ResponseEntity.ok().body(Map.of("key", key, "value", value));
    }
}
