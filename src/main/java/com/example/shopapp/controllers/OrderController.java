package com.example.shopapp.controllers;

import com.example.shopapp.dtos.OrderDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {

    @PostMapping("")
    public ResponseEntity<Object> createOrder(
            @Valid
            @RequestBody OrderDTO orderDTO,
            BindingResult result
            ) {
        try{
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body("Validation errors: " + String.join(", ", errors));
            }
            return ResponseEntity.ok("Order success");
        }catch(Exception e){
            return ResponseEntity.badRequest().body("Order failed");
        }
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<String> getOrders(@Valid @PathVariable("user_id") Integer userId) {
        return ResponseEntity.ok("Chao e iu hi getOrdersByUserId " + userId);
    }

    @PutMapping("/{user_id}")
    public ResponseEntity<String> updateOrder(
            @PathVariable("user_id") Integer userId,
            @Valid @RequestBody OrderDTO orderDTO
    ) {
        return ResponseEntity.ok("Update received for user ID: " + userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(
            @Valid
            @PathVariable("id") Integer id
    ) {
        // a softly delete
        return ResponseEntity.ok("Delete received : " + id);
    }
}
