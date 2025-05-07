package com.fudan.xiaozhong_dianping.groupbuy.builder;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券构建器
 */
public class CouponBuilder {
    private final Coupon coupon;
    
    public CouponBuilder() {
        this.coupon = new Coupon();
    }
    
    // 必填字段
    public CouponBuilder title(String title) {
        coupon.setTitle(title);
        return this;
    }
    
    public CouponBuilder type(CouponType type) {
        coupon.setType(type.getDescription());
        return this;
    }
    
    // 可选字段
    public CouponBuilder description(String description) {
        coupon.setDescription(description);
        return this;
    }
    
    public CouponBuilder amount(BigDecimal amount) {
        coupon.setAmount(amount);
        return this;
    }
    
    public CouponBuilder maxDeduction(BigDecimal maxDeduction) {
        coupon.setMaxDeduction(maxDeduction);
        return this;
    }
    
    public CouponBuilder useThreshold(BigDecimal useThreshold) {
        coupon.setUseThreshold(useThreshold);
        return this;
    }
    
    public CouponBuilder applicableCategory(String applicableCategory) {
        coupon.setApplicableCategory(applicableCategory);
        return this;
    }
    
    public CouponBuilder applicableShop(String applicableShop) {
        coupon.setApplicableShop(applicableShop);
        return this;
    }
    
    public CouponBuilder expirationDate(LocalDateTime expirationDate) {
        coupon.setExpirationDate(expirationDate);
        return this;
    }
    
    public CouponBuilder validDays(Integer validDays) {
        coupon.setValidDays(validDays);
        return this;
    }
    
    public CouponBuilder totalQuantity(Integer totalQuantity) {
        coupon.setTotalQuantity(totalQuantity);
        return this;
    }
    
    public CouponBuilder maxPerUser(Integer maxPerUser) {
        coupon.setMaxPerUser(maxPerUser);
        return this;
    }
    
    public CouponBuilder isNewUserCoupon(boolean isNewUserCoupon) {
        coupon.setNewUserCoupon(isNewUserCoupon);
        return this;
    }
    
    // 验证和构建
    public Coupon build() {
        validate();
        return coupon;
    }
    
    private void validate() {
        if (coupon.getTitle() == null || coupon.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("优惠券标题不能为空");
        }
        if (coupon.getType() == null) {
            throw new IllegalArgumentException("优惠券类型不能为空");
        }
        // 根据不同类型验证必填字段
        validateByType();
    }
    
    private void validateByType() {
        CouponType type = CouponType.fromDescription(coupon.getType());
        switch (type) {
            case FIXED_AMOUNT:
            case FIXED_PRICE:
                if (coupon.getAmount() == null) {
                    throw new IllegalArgumentException("固定金额优惠券必须设置金额");
                }
                if (coupon.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("优惠金额必须大于0");
                }
                break;
            case DISCOUNT:
                if (coupon.getAmount() == null) {
                    throw new IllegalArgumentException("折扣券必须设置折扣率");
                }
                if (coupon.getAmount().compareTo(BigDecimal.ZERO) <= 0 || 
                    coupon.getAmount().compareTo(BigDecimal.ONE) >= 0) {
                    throw new IllegalArgumentException("折扣率必须在0-1之间");
                }
                break;
        }
    }
} 