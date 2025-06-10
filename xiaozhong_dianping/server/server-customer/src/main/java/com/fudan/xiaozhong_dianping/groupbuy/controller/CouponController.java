package com.fudan.xiaozhong_dianping.groupbuy.controller;

import com.fudan.xiaozhong_dianping.groupbuy.dto.CouponDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        try {
            System.out.println("===== 新人领取优惠券请求 =====");
            System.out.println("用户ID: " + userId);
            System.out.println("优惠券ID: " + couponId);
            
            // 检查用户是否为新用户
            boolean isNewUser = couponService.isNewUser(userId);
            System.out.println("是否为新用户: " + isNewUser);
            
            if (!isNewUser) {
                throw new BusinessException("您不是新用户，无法领取新人券");
            }
            
            // 检查优惠券是否存在
            boolean hasReceivedCoupon = couponService.hasReceivedNewUserCoupon(userId);
            System.out.println("是否已领取过新人券: " + hasReceivedCoupon);
            
            UserCoupon userCoupon = couponService.receiveCoupon(userId, couponId);
            System.out.println("领取成功，用户优惠券ID: " + userCoupon.getId());
            
            return ResponseEntity.ok(userCoupon);
        } catch (Exception e) {
            System.out.println("===== 新人领取优惠券异常 =====");
            System.out.println("异常类型: " + e.getClass().getName());
            System.out.println("异常信息: " + e.getMessage());
            e.printStackTrace();
            
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("领取优惠券失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取新人券列表
     * @return 优惠券DTO列表
     */
    @GetMapping("/new-user-coupons")
    public ResponseEntity<List<CouponDTO>> getNewUserCoupons() {
        List<Coupon> coupons = couponService.getNewUserCoupons();
        
        // 转换为DTO
        List<CouponDTO> couponDTOs = coupons.stream()
                .map(coupon -> {
                    CouponDTO dto = new CouponDTO();
                    BeanUtils.copyProperties(coupon, dto);
                    
                    // 将isNewUserCoupon转为前端期望的newUserCoupon字段
                    dto.setNewUserCoupon(coupon.isNewUserCoupon());
                    
                    // 输出调试信息
                    System.out.println("发送优惠券到前端: ID=" + dto.getId() + ", 标题=" + dto.getTitle() + ", 新人券标志=" + dto.isNewUserCoupon());
                    
                    return dto;
                })
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(couponDTOs);
    }
    
    /**
     * 检查用户是否为新用户
     * @param userId 用户ID
     * @return 是否为新用户
     */
    @GetMapping("/is-new-user")
    public ResponseEntity<Boolean> isNewUser(@RequestHeader("userId") Long userId) {
        boolean isNewUser = couponService.isNewUser(userId);
        return ResponseEntity.ok(isNewUser);
    }
    
    /**
     * 检查用户是否已领取新人券
     * @param userId 用户ID
     * @return 是否已领取新人券
     */
    @GetMapping("/has-received-new-user-coupon")
    public ResponseEntity<Boolean> hasReceivedNewUserCoupon(@RequestHeader("userId") Long userId) {
        boolean hasReceived = couponService.hasReceivedNewUserCoupon(userId);
        return ResponseEntity.ok(hasReceived);
    }
}