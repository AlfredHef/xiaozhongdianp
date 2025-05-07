package com.fudan.xiaozhong_dianping.groupbuy.strategy;

import java.math.BigDecimal;

/**
 * 优惠券折扣计算策略接口
 */
public interface DiscountStrategy {

    /**
     * 计算优惠券的折扣金额
     * @param orderPrice 订单原价
     * @param couponValue 优惠券面值（可能是金额或折扣率）
     * @param threshold 使用门槛
     * @param maxDeduction 最大抵扣金额
     * @return 折扣金额
     */
    BigDecimal calculateDiscount(BigDecimal orderPrice, BigDecimal couponValue,
                                 BigDecimal threshold, BigDecimal maxDeduction);
}