package com.example.shopapp.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "coupon_conditions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    @JsonBackReference
    private Coupon coupon;

    @Column(name = "attribute", nullable = false)
    private String attribute;

    @Column(name = "operator", nullable = false)
    private String operator;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;
}
/*

1 coupon has many order
1 coupon has many order_detail
1 coupon has many coupon_condition

INSERT INTO coupons(id, code) VALUES (1, 'HEAVEN');
INSERT INTO coupons(id, code) VALUES (2, 'DISCOUNT20');

INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
VALUES (1, 1, 'minimum_amount', '>', '100', 10);

INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
VALUES (2, 1, 'applicable_date', 'BETWEEN', '2023-12-25', 5);

INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
VALUES (3, 2, 'minimum_amount', '>', '200', 20);

Nếu đơn hàng có tổng giá trị là 120 đô la và áp dụng coupon 1
Giá trị sau khi áp dụng giảm giá 10%:
120 đô la * (1 - 10/100) = 120 đô la * 0.9 = 108 đô la

Giá trị sau khi áp dụng giảm giá 5%:
108 đô la * (1 - 5/100) = 108 đô la * 0.95 = 102.6 đô la
* */