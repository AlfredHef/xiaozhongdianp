package com.fudan.xiaozhong_dianping.groupbuy.validator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券验证实现类
 * 用于校验优惠券的有效期、使用门槛、适用店铺、适用品类和使用状态
 */
public class CouponValidatorImpl implements CouponValidator {

    /**
     * 验证优惠券是否有效
     * 包含过期验证、使用状态验证等
     *
     * @param couponId 优惠券ID
     * @param userId 用户ID
     * @return 是否有效
     */
    @Override
    public boolean isValid(Long couponId, Long userId) {
        
        return !isExpired(couponId) && !isUsed(couponId, userId);
    }

    /**
     * 验证优惠券是否适用于特定商品或套餐
     * 包含适用品类验证、适用店铺验证、使用门槛验证等
     *
     * @param couponId 优惠券ID
     * @param targetId 目标商品或套餐ID
     * @return 是否适用
     */
    @Override
    public boolean isApplicable(Long couponId, Long targetId) {
        
        return isCategoryApplicable(couponId, targetId) 
                && isShopApplicable(couponId, targetId)
                && isThresholdMet(couponId, targetId);
    }

    /**
     * 获取优惠券详细信息
     *
     * @param couponId 优惠券ID
     * @return 优惠券详细信息
     */
    @Override
    public Map<String, Object> getCouponDetails(Long couponId) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("id", couponId);
        details.put("isExpired", isExpired(couponId));
        details.put("threshold", getThreshold(couponId));
        details.put("applicableShops", getApplicableShops(couponId));
        details.put("applicableCategories", getApplicableCategories(couponId));
        
        return details;
    }

    /**
     * 计算使用优惠券后的最终价格
     *
     * @param originalPrice 原始价格
     * @param couponId 优惠券ID
     * @return 最终价格
     */
    @Override
    public double calculateFinalPrice(double originalPrice, Long couponId) {
       
        String couponType = getCouponType(couponId);
        double discountAmount = getDiscountAmount(couponId);
        
        if ("折扣券".equals(couponType)) {
            return originalPrice * discountAmount;
        } else if ("满减券".equals(couponType)) {
            double threshold = getThreshold(couponId);
            if (originalPrice >= threshold) {
                return Math.max(0, originalPrice - discountAmount);
            }
        } else if ("直减券".equals(couponType)) {
            return Math.max(0, originalPrice - discountAmount);
        }
        
        return originalPrice;
    }
    
    /**
     * 检查优惠券是否过期
     *
     * @param couponId 优惠券ID
     * @return 是否过期
     */
    private boolean isExpired(Long couponId) {
        
        Date now = new Date();
        Date expirationDate = getExpirationDate(couponId);
        
        if (expirationDate == null) {
            // 无过期时间的优惠券视为未过期
            return false;
        }
        
        return now.after(expirationDate);
    }
    
    /**
     * 检查优惠券是否已使用
     *
     * @param couponId 优惠券ID
     * @param userId 用户ID
     * @return 是否已使用
     */
    private boolean isUsed(Long couponId, Long userId) {
        
        return false;
    }
    
    /**
     * 检查优惠券适用品类是否匹配
     *
     * @param couponId 优惠券ID
     * @param targetId 目标商品或套餐ID
     * @return 是否匹配适用品类
     */
    private boolean isCategoryApplicable(Long couponId, Long targetId) {
        
        String[] applicableCategories = getApplicableCategories(couponId);
        
        if (applicableCategories == null || applicableCategories.length == 0) {
            // 无限制品类的优惠券对所有品类都适用
            return true;
        }
        
        String targetCategory = getTargetCategory(targetId);
        for (String category : applicableCategories) {
            if (category.equals(targetCategory)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查优惠券适用店铺是否匹配
     *
     * @param couponId 优惠券ID
     * @param targetId 目标商品或套餐ID
     * @return 是否匹配适用店铺
     */
    private boolean isShopApplicable(Long couponId, Long targetId) {
        
        Long[] applicableShops = getApplicableShops(couponId);
        
        if (applicableShops == null || applicableShops.length == 0) {
            // 无限制店铺的优惠券对所有店铺都适用
            return true;
        }
        
        Long targetShop = getTargetShop(targetId);
        for (Long shop : applicableShops) {
            if (shop.equals(targetShop)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否满足优惠券使用门槛
     *
     * @param couponId 优惠券ID
     * @param targetId 目标商品或套餐ID
     * @return 是否满足使用门槛
     */
    private boolean isThresholdMet(Long couponId, Long targetId) {
        
        double threshold = getThreshold(couponId);
        double targetPrice = getTargetPrice(targetId);
        
        if (threshold <= 0) {
            // 无门槛优惠券对任何价格都适用
            return true;
        }
        
        return targetPrice >= threshold;
    }
    
    // 以下是模拟数据获取的辅助方法，实际实现中应该从数据库查询
    
    private Date getExpirationDate(Long couponId) {
        
        return null;
    }
    
    private String[] getApplicableCategories(Long couponId) {
        
        return new String[0];
    }
    
    private Long[] getApplicableShops(Long couponId) {
        
        return new Long[0];
    }
    
    private double getThreshold(Long couponId) {
       
        return 0;
    }
    
    private String getTargetCategory(Long targetId) {
        
        return "通用";
    }
    
    private Long getTargetShop(Long targetId) {
        
        return 1L;
    }
    
    private double getTargetPrice(Long targetId) {
        
        return 100.0;
    }
    
    private String getCouponType(Long couponId) {
        
        return "折扣券";
    }
    
    private double getDiscountAmount(Long couponId) {
        
        return 0.8; // 8折
    }
} 