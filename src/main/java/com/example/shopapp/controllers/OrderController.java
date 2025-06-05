package com.example.shopapp.controllers;

import com.example.shopapp.dtos.request.OrderDTO;
import com.example.shopapp.dtos.responses.OrderListResponse;
import com.example.shopapp.dtos.responses.OrderResponse;
import com.example.shopapp.models.Order;
import com.example.shopapp.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @PostMapping("")
    public ResponseEntity<Object> createOrder(
            @Valid
            @RequestBody OrderDTO orderDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body("Validation errors: " + String.join(", ", errors));
            }
            OrderResponse orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 1 userID has many orders
    @GetMapping("/user/{user_id}")
    public ResponseEntity<Object> getOrders(@Valid @PathVariable("user_id") Integer userId) {
        List<Order> orders = orderService.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrder(@Valid @PathVariable("id") Integer id) {
        Order existingOrder = orderService.getOrderById(id);
        return ResponseEntity.ok(OrderResponse.fromOrder(existingOrder));
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

    // ✅ Get all orders by keyword (with pagination)
    @GetMapping("/get-order-by-keyword")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<OrderListResponse> searchOrders(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("Info log message");
        //logger.error("Error occurred", new RuntimeException("Dummy exception"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        // Get paginated orders and map to DTOs
        Page<OrderResponse> orderPage = orderService
                .getOrdersByKeyword(keyword, pageable)
                .map(OrderResponse::fromOrder);

        List<OrderResponse> orderResponses = orderPage.getContent();

        OrderListResponse response = OrderListResponse.builder()
                .message("Fetched orders successfully")
                .page(page)
                .limit(size)
                .totalPages(orderPage.getTotalPages())
                .totalItems(orderPage.getTotalElements())
                .orders(orderResponses)
                .build();

        return ResponseEntity.ok(response);
    }



}
