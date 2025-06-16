package com.example.shopapp.controllers;


import com.example.shopapp.dtos.responses.CouponResultDTO;
import com.example.shopapp.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @GetMapping("/apply")
    public ResponseEntity<CouponResultDTO> applyCoupon(
            @RequestParam("code") String couponCode,
            @RequestParam("total") double totalAmount
    ) {
        CouponResultDTO result = couponService.calculateCouponValue(couponCode, totalAmount);
        return ResponseEntity.ok(result);
    }
}

