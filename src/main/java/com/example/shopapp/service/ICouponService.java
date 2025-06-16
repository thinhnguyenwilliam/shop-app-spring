package com.example.shopapp.service;


import com.example.shopapp.dtos.responses.CouponResultDTO;

public interface ICouponService {
    CouponResultDTO calculateCouponValue(String couponCode, double totalAmount);
}


