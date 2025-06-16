package com.example.shopapp.service.impl;

import com.example.shopapp.dtos.responses.CouponResultDTO;
import com.example.shopapp.enums.CouponAttribute;
import com.example.shopapp.enums.CouponOperator;
import com.example.shopapp.models.Coupon;
import com.example.shopapp.models.CouponCondition;
import com.example.shopapp.repositories.CouponConditionRepository;
import com.example.shopapp.repositories.CouponRepository;
import com.example.shopapp.service.ICouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponService implements ICouponService {
    private final CouponRepository couponRepository;
    private final CouponConditionRepository couponConditionRepository;

    @Override
    public CouponResultDTO calculateCouponValue(String couponCode, double totalAmount) {
        try {
            Coupon coupon = getValidCouponOrThrow(couponCode);

            List<CouponCondition> conditions = couponConditionRepository.findByCouponId(coupon.getId());
            double discount = 0.0;
            List<String> appliedConditions = new ArrayList<>();

            for (CouponCondition condition : conditions) {
                discount += applyCondition(condition, totalAmount, appliedConditions);
            }

            double finalAmount = totalAmount - discount;
            return new CouponResultDTO(totalAmount, discount, finalAmount, appliedConditions);

        } catch (IllegalArgumentException e) {
            return new CouponResultDTO(
                    totalAmount,
                    0.0,
                    totalAmount,
                    List.of(e.getMessage())
            );
        }
    }

    private Coupon getValidCouponOrThrow(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with code: " + code));
        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }
        return coupon;
    }

    private double applyCondition(CouponCondition condition, double totalAmount, List<String> appliedConditions) {
        CouponAttribute attribute = CouponAttribute.safeValueOf(condition.getAttribute());
        CouponOperator operator = CouponOperator.safeFromSymbol(condition.getOperator());

        if (attribute == null || operator == null) return 0.0;

        double percentDiscount = condition.getDiscountAmount().doubleValue();

        return switch (attribute) {
            case MINIMUM_AMOUNT ->
                    handleMinimumAmount(condition, operator, totalAmount, percentDiscount, appliedConditions);
            case APPLICABLE_DATE ->
                    handleApplicableDate(condition, operator, totalAmount, percentDiscount, appliedConditions);
        };
    }

    private double handleMinimumAmount(CouponCondition condition, CouponOperator operator, double totalAmount,
                                       double percentDiscount, List<String> appliedConditions) {
        double threshold = Double.parseDouble(condition.getValue());
        if (operator == CouponOperator.GREATER_THAN && totalAmount > threshold) {
            appliedConditions.add("minimum_amount condition applied");
            return totalAmount * percentDiscount / 100;
        }
        return 0.0;
    }


    private double handleApplicableDate(CouponCondition condition, CouponOperator operator, double totalAmount,
                                        double percentDiscount, List<String> appliedConditions) {
        LocalDate applicableDate = LocalDate.parse(condition.getValue());
        //LocalDate currentDate = LocalDate.now(); // Or LocalDate.of(2025, 12, 25) for testing
        LocalDate currentDate = LocalDate.of(2023, 12, 25);
        if (operator == CouponOperator.BETWEEN && currentDate.isEqual(applicableDate)) {
            appliedConditions.add("applicable_date condition applied");
            return totalAmount * percentDiscount / 100;
        }
        return 0.0;
    }




}
