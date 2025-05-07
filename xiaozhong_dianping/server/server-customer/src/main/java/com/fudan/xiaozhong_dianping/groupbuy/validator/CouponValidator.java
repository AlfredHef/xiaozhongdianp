package com.fudan.xiaozhong_dianping.groupbuy.validator;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Order;

/**
 * 优惠券校验器接口
 */
public interface CouponValidator {
    /**
     * 校验优惠券是否可用
     * @param coupon 优惠券
     * @param order 订单
     * @return 校验结果
     */
    ValidationResult validate(Coupon coupon, Order order);
    
    /**
     * 设置下一个校验器
     * @param next 下一个校验器
     * @return 下一个校验器
     */
    CouponValidator setNext(CouponValidator next);
} 