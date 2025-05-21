package com.example.shopapp.controllers;


import com.example.shopapp.dtos.request.OrderDetailDTO;
import com.example.shopapp.dtos.responses.OrderDetailResponse;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.service.IOrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {

    private final IOrderDetailService orderDetailService;

    @PostMapping
    public ResponseEntity<Object> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
    )
    {
        OrderDetail newOrderDetail= orderDetailService.createOrderDetail(orderDetailDTO);
        return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(newOrderDetail));
    }

    // get list order_details of 1 order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Object> getOrderDetails(
            @Valid @PathVariable("orderId") Integer id
    )
    {
        List<OrderDetail> orderDetails= orderDetailService.findAllByOrderId(id);
        List<OrderDetailResponse> orderDetailResponses=orderDetails.stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .toList();
        return ResponseEntity.ok(orderDetailResponses);
    }



    @GetMapping("/{orderId}")
    public ResponseEntity<Object> getOrderDetailsByOrderId(
            @Valid @PathVariable("orderId") Integer id
    )
    {
        OrderDetail orderDetail= orderDetailService.getOrderDetailById(id);
        return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(orderDetail));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Object> updateOrderDetails(
            @Valid @PathVariable("orderId") Integer id,
            @RequestBody OrderDetailDTO newOrderDetailDTO
    )
    {
        OrderDetail orderDetail =orderDetailService.updateOrderDetail(id, newOrderDetailDTO);
        return ResponseEntity.ok(orderDetail);
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<Object> deleteOrderDetails(
            @Valid @PathVariable("orderId") Integer id
    )
    {
        orderDetailService.deleteOrderDetailById(id);
        return ResponseEntity.ok("Chao e iu hi From order to deleteOrderDetails " + id);
    }
}
