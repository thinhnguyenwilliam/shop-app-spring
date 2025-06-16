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
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponService implements ICouponService {
    private final CouponRepository couponRepository;
    private final CouponConditionRepository couponConditionRepository;

    @Override
    public CouponResultDTO calculateCouponValue(String couponCode, double totalAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with code: " + couponCode));

        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }

        List<CouponCondition> conditions = couponConditionRepository.findByCouponId(coupon.getId());
        double discount = 0.0;
        List<String> appliedConditions = new ArrayList<>();

        for (CouponCondition condition : conditions) {
            try {
                CouponAttribute attribute = CouponAttribute.safeValueOf(condition.getAttribute());
                CouponOperator operator = CouponOperator.safeFromSymbol(condition.getOperator());
                double percentDiscount = condition.getDiscountAmount().doubleValue();


                switch (Objects.requireNonNull(attribute)) {
                    case MINIMUM_AMOUNT:
                        double threshold = Double.parseDouble(condition.getValue());
                        if (operator == CouponOperator.GREATER_THAN && totalAmount > threshold) {
                            discount += totalAmount * percentDiscount / 100;
                            appliedConditions.add("minimum_amount condition applied");
                        }
                        break;

                    case APPLICABLE_DATE:
                        LocalDate applicableDate = LocalDate.parse(condition.getValue());
                        //LocalDate currentDate = LocalDate.now();
                        LocalDate currentDate = LocalDate.of(2024, 12, 25);

                        if (operator == CouponOperator.BETWEEN && currentDate.isEqual(applicableDate)) {
                            discount += totalAmount * percentDiscount / 100;
                            appliedConditions.add("applicable_date condition applied");
                        }
                        break;

                    // Add more attributes here...

                    default:
                        break;
                }
            } catch (Exception e) {
                // Log the error and skip this condition
                log.info("Skipping condition due to error: {}",e.getMessage());
            }
        }

        double finalAmount = totalAmount - discount;
        return new CouponResultDTO(totalAmount, discount, finalAmount, appliedConditions);
    }
}
