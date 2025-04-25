package com.fudan.xiaozhong_dianping.groupbuy.controller;

import com.fudan.xiaozhong_dianping.groupbuy.dto.CouponDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券相关接口
 */
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 新用户领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券实体
     */
    @PostMapping("/receive")
    public ResponseEntity<UserCoupon> receiveCoupon(
            @RequestHeader("userId") Long userId,
            @RequestParam Long couponId) {
        UserCoupon userCoupon = couponService.receiveCoupon(userId, couponId);
        return ResponseEntity.ok(userCoupon);
    }

    /**
     * 获取用户卡包中的优惠券
     * @param userId 用户ID
     * @return 优惠券DTO列表
     */
    @GetMapping("/wallet")
    public ResponseEntity<List<CouponDTO>> getCouponsInUserWallet(
            @RequestHeader("userId") Long userId) {
        List<CouponDTO> coupons = couponService.getCouponsInUserWallet(userId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 获取用户可用的优惠券列表
     * @param userId 用户ID
     * @return 优惠券DTO列表
     */
    @GetMapping("/user")
    public ResponseEntity<List<CouponDTO>> getUserAvailableCoupons(
            @RequestHeader("userId") Long userId) {
        if (userId == null) {
            throw new BusinessException("用户未登录，请先登录");
        }
        List<CouponDTO> coupons = couponService.getUserAvailableCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 新用户领取新人券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券实体
     */
    @PostMapping("/receive-new-user-coupon")
    public ResponseEntity<UserCoupon> receiveNewUserCoupon(
            @RequestHeader("userId") Long userId,
            @RequestParam Long couponId) {
        if (!couponService.isNewUser(userId)) {
            throw new BusinessException("您不是新用户，无法领取新人券");
        }
        UserCoupon userCoupon = couponService.receiveCoupon(userId, couponId);
        return ResponseEntity.ok(userCoupon);
    }
}