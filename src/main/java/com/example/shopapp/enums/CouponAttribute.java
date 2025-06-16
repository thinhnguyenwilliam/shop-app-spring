package com.example.shopapp.enums;


public enum CouponAttribute {
    MINIMUM_AMOUNT,
    APPLICABLE_DATE;

    public static CouponAttribute safeValueOf(String name) {
        try {
            return CouponAttribute.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}


