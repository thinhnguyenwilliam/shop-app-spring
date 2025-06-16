package com.example.shopapp.enums;


import lombok.Getter;

@Getter
public enum CouponOperator {
    GREATER_THAN(">"),
    BETWEEN("BETWEEN");

    private final String symbol;

    CouponOperator(String symbol) {
        this.symbol = symbol;
    }

    public static CouponOperator safeFromSymbol(String symbol) {
        for (CouponOperator op : values()) {
            if (op.symbol.equalsIgnoreCase(symbol)) {
                return op;
            }
        }
        return null;
    }
}
