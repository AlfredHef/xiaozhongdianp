package com.fudan.xiaozhong_dianping.groupbuy.validator;

import java.util.Map;

/**
 * 优惠券验证接口
 * 用于验证优惠券的有效性和适用性
 */
public interface CouponValidator {
    
    /**
     * 验证优惠券是否有效
     * 
     * @param couponId 优惠券ID
     * @param userId 用户ID
     * @return 是否有效
     */
    boolean isValid(Long couponId, Long userId);
    
    /**
     * 验证优惠券是否适用于特定商品或套餐
     * 
     * @param couponId 优惠券ID
     * @param targetId 目标商品或套餐ID
     * @return 是否适用
     */
    boolean isApplicable(Long couponId, Long targetId);
    
    /**
     * 获取优惠券详细信息
     * 
     * @param couponId 优惠券ID
     * @return 优惠券详细信息
     */
    Map<String, Object> getCouponDetails(Long couponId);
    
    /**
     * 计算使用优惠券后的最终价格
     * 
     * @param originalPrice 原始价格
     * @param couponId 优惠券ID
     * @return 最终价格
     */
    double calculateFinalPrice(double originalPrice, Long couponId);
} 