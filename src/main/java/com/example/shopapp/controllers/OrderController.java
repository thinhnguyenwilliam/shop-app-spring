package com.example.shopapp.controllers;

import com.example.shopapp.dtos.request.OrderDTO;
import com.example.shopapp.dtos.responses.OrderResponse;
import com.example.shopapp.models.Order;
import com.example.shopapp.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;


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
            OrderResponse orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(orderResponse);
        }catch(Exception e){
            return ResponseEntity.badRequest().body("Order failed");
        }
    }

    // 1 userID has many orders
    @GetMapping("/user/{user_id}")
    public ResponseEntity<Object> getOrders(@Valid @PathVariable("user_id") Integer userId)
    {
        List<Order> orders= orderService.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrder(@Valid @PathVariable("id") Integer id)
    {
        Order existingOrder= orderService.getOrderById(id);
        return ResponseEntity.ok(existingOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOrder(
            @PathVariable("id") Integer id,
            @Valid @RequestBody OrderDTO orderDTO
    ) {
        Order order = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(
            @Valid
            @PathVariable("id") Integer id
    ) {
        // a softly delete
        orderService.deleteOrderById(id);
        return ResponseEntity.ok("Delete received : " + id);
    }
}
