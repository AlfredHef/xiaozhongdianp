package com.fudan.xiaozhong_dianping.groupbuy.factory;

import com.fudan.xiaozhong_dianping.groupbuy.builder.CouponBuilder;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券工厂类
 */
public class CouponFactory {
    
    public static CouponBuilder createBuilder() {
        return new CouponBuilder();
    }
    
    /**
     * 创建固定金额优惠券
     */
    public static Coupon createFixedAmountCoupon(String title, BigDecimal amount) {
        return createBuilder()
            .title(title)
            .type(CouponType.FIXED_AMOUNT)
            .amount(amount)
            .build();
    }
    
    /**
     * 创建折扣券
     */
    public static Coupon createDiscountCoupon(String title, BigDecimal discountRate) {
        return createBuilder()
            .title(title)
            .type(CouponType.DISCOUNT)
            .amount(discountRate)
            .build();
    }
    
    /**
     * 创建新人专享券
     */
    public static Coupon createNewUserCoupon(String title, BigDecimal amount) {
        return createBuilder()
            .title(title)
            .type(CouponType.FIXED_AMOUNT)
            .amount(amount)
            .description("新用户专享优惠")
            .isNewUserCoupon(true)
            .validDays(30)  // 默认30天有效期
            .build();
    }
    
    /**
     * 创建满减券
     */
    public static Coupon createThresholdCoupon(String title, BigDecimal threshold, BigDecimal amount) {
        return createBuilder()
            .title(title)
            .type(CouponType.FIXED_AMOUNT)
            .amount(amount)
            .useThreshold(threshold)
            .description("满" + threshold + "减" + amount)
            .build();
    }
} 