package com.fudan.xiaozhong_dianping.groupbuy.validator.impl;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Order;
import com.fudan.xiaozhong_dianping.groupbuy.validator.AbstractCouponValidator;
import com.fudan.xiaozhong_dianping.groupbuy.validator.ValidationResult;

/**
 * 使用状态校验器
 */
public class UsageStatusValidator extends AbstractCouponValidator {
    @Override
    protected ValidationResult doValidate(Coupon coupon, Order order) {
        // 检查优惠券是否已被使用
        if (coupon.getStatus() != null && coupon.getStatus() == 1) {
            return ValidationResult.failure("优惠券已被使用");
        }
        return ValidationResult.success();
    }
} 