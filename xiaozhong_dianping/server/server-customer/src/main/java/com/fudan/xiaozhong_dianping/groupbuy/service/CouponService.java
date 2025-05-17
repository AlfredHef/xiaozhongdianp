package com.fudan.xiaozhong_dianping.groupbuy.service;

import com.fudan.xiaozhong_dianping.groupbuy.dto.CouponDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 新用户领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券实体
     */
    UserCoupon receiveCoupon(Long userId, Long couponId);

    /**
     * 获取用户卡包中的优惠券
     * @param userId 用户ID
     * @return 优惠券DTO列表
     */
    List<CouponDTO> getCouponsInUserWallet(Long userId);

    /**
     * 获取用户所有可用的优惠券
     * @param userId 用户ID
     * @return 优惠券DTO列表
     */
    List<CouponDTO> getUserAvailableCoupons(Long userId);

    /**
     * 获取可用的优惠券
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param orderPrice 订单价格
     * @return 优惠券列表
     */
    List<Coupon> getAvailableCoupons(Long userId, Integer packageId, BigDecimal orderPrice);

    /**
     * 获取减免金额最高的优惠券
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param orderPrice 订单价格
     * @return 优惠券
     */
    Coupon getMaxDiscountCoupon(Long userId, Integer packageId, BigDecimal orderPrice);

    /**
     * 验证用户是否为新用户
     * @param userId 用户ID
     * @return 是否为新用户
     */
    boolean isNewUser(Long userId);

    /**
     * 验证用户是否已领取过新人券
     * @param userId 用户ID
     * @return 是否已领取过新人券
     */
    boolean hasReceivedNewUserCoupon(Long userId);

    /**
     * 获取新人券列表
     * @return 新人券列表
     */
    List<Coupon> getNewUserCoupons();

    /**
     * 计算优惠券折扣金额
     * @param coupon 优惠券实体
     * @param orderPrice 订单价格
     * @return 折扣金额
     */
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderPrice);

    /**
     * 检查用户是否已获得点评奖励券
     */
    boolean hasReceivedReviewReward(Long userId);

    /**
     * 发放点评奖励券（8折，最高抵扣20元，7天有效）
     */
    void grantReviewRewardCoupon(Long userId);

    /**
     * 创建新的优惠券
     * @param coupon 优惠券信息
     * @return 创建的优惠券
     */
    Coupon createCoupon(Coupon coupon);
}