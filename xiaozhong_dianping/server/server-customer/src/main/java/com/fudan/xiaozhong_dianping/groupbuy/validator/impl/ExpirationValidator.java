package com.fudan.xiaozhong_dianping.groupbuy.validator.impl;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Order;
import com.fudan.xiaozhong_dianping.groupbuy.validator.AbstractCouponValidator;
import com.fudan.xiaozhong_dianping.groupbuy.validator.ValidationResult;

import java.time.LocalDateTime;

/**
 * 过期校验器
 */
public class ExpirationValidator extends AbstractCouponValidator {
    @Override
    protected ValidationResult doValidate(Coupon coupon, Order order) {
        if (coupon.getExpirationDate() != null && 
            LocalDateTime.now().isAfter(coupon.getExpirationDate())) {
            return ValidationResult.failure("优惠券已过期");
        }
        return ValidationResult.success();
    }
} 