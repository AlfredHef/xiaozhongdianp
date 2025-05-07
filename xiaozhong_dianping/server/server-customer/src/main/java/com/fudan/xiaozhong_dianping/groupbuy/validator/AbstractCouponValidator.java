package com.fudan.xiaozhong_dianping.groupbuy.validator;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Order;

/**
 * 抽象优惠券校验器
 */
public abstract class AbstractCouponValidator implements CouponValidator {
    protected CouponValidator next;
    
    @Override
    public CouponValidator setNext(CouponValidator next) {
        this.next = next;
        return next;
    }
    
    @Override
    public ValidationResult validate(Coupon coupon, Order order) {
        // 执行当前校验
        ValidationResult result = doValidate(coupon, order);
        
        // 如果校验失败，直接返回结果
        if (!result.isValid()) {
            return result;
        }
        
        // 如果还有下一个校验器，继续校验
        if (next != null) {
            return next.validate(coupon, order);
        }
        
        // 所有校验都通过
        return ValidationResult.success();
    }
    
    /**
     * 执行具体的校验逻辑
     * @param coupon 优惠券
     * @param order 订单
     * @return 校验结果
     */
    protected abstract ValidationResult doValidate(Coupon coupon, Order order);
} 