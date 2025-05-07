package com.fudan.xiaozhong_dianping.groupbuy.validator.impl;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Order;
import com.fudan.xiaozhong_dianping.groupbuy.validator.AbstractCouponValidator;
import com.fudan.xiaozhong_dianping.groupbuy.validator.ValidationResult;

import java.math.BigDecimal;

/**
 * 使用门槛校验器
 */
public class ThresholdValidator extends AbstractCouponValidator {
    @Override
    protected ValidationResult doValidate(Coupon coupon, Order order) {
        if (coupon.getUseThreshold() != null && 
            order.getTotalAmount().compareTo(coupon.getUseThreshold()) < 0) {
            return ValidationResult.failure(
                String.format("订单金额未达到使用门槛，需要满%s元", coupon.getUseThreshold())
            );
        }
        return ValidationResult.success();
    }
} 