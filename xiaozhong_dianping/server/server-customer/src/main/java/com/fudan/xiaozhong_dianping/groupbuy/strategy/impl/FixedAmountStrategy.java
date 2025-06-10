package com.fudan.xiaozhong_dianping.groupbuy.strategy.impl;

import com.fudan.xiaozhong_dianping.groupbuy.strategy.DiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 满减券策略实现
 */
@Component
public class FixedAmountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderPrice, BigDecimal couponValue,
                                        BigDecimal threshold, BigDecimal maxDeduction) {
        // 如果有门槛且订单金额小于门槛，则无法使用
        if (threshold != null && orderPrice.compareTo(threshold) < 0) {
            return BigDecimal.ZERO;
        }

        return couponValue; // 直接返回优惠券固定金额
    }
}
