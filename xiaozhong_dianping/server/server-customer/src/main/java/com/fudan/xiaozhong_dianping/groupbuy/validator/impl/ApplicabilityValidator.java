package com.fudan.xiaozhong_dianping.groupbuy.validator.impl;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Order;
import com.fudan.xiaozhong_dianping.groupbuy.validator.AbstractCouponValidator;
import com.fudan.xiaozhong_dianping.groupbuy.validator.ValidationResult;

/**
 * 适用范围校验器
 */
public class ApplicabilityValidator extends AbstractCouponValidator {
    @Override
    protected ValidationResult doValidate(Coupon coupon, Order order) {
        // 检查适用品类
        if (coupon.getApplicableCategory() != null && 
            !coupon.getApplicableCategory().equals(order.getCategory())) {
            return ValidationResult.failure("优惠券不适用于当前商品品类");
        }
        
        // 检查适用店铺
        if (coupon.getApplicableShop() != null && 
            !coupon.getApplicableShop().equals(order.getShopId())) {
            return ValidationResult.failure("优惠券不适用于当前店铺");
        }
        
        return ValidationResult.success();
    }
} 