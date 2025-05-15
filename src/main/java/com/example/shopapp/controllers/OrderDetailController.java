package com.example.shopapp.controllers;


import com.example.shopapp.dtos.OrderDetailDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/order_details")
public class OrderDetailController {

    @PostMapping
    public ResponseEntity<Object> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
    )
    {
        return ResponseEntity.ok("Order detail received");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Object> getOrderDetails(
            @Valid @PathVariable("orderId") Integer id
    )
    {
        return ResponseEntity.ok("Chao e iu hi getOrderDetailsByOrderId " + id);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Object> getOrderDetailsByOrderId(
            @Valid @PathVariable("orderId") Integer id
    )
    {
        return ResponseEntity.ok("Chao e iu hi From order to getOrderDetailsByOrderId " + id);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Object> updateOrderDetails(
            @Valid @PathVariable("orderId") Integer id,
            @RequestBody OrderDetailDTO newOrderDetailDTO
    )
    {
        return ResponseEntity.ok("Chao e iu hi From order to updateOrderDetails " + id);
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<Object> deleteOrderDetails(
            @Valid @PathVariable("orderId") Integer id
    )
    {
        return ResponseEntity.ok("Chao e iu hi From order to deleteOrderDetails " + id);
    }
}
