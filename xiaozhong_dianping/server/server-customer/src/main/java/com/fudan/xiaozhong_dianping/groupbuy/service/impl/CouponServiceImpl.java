package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.dto.CouponDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.repository.CouponRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyOrderRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.UserCouponRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 */
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private GroupBuyPackageRepository packageRepository;

    @Autowired
    private GroupBuyOrderRepository orderRepository;

    @Override
    public UserCoupon receiveCoupon(Long userId, Long couponId) {
        if (isNewUser(userId)) {
            if (hasReceivedNewUserCoupon(userId)) {
                throw new BusinessException("您已领取过新人券，不能再领取");
            }
        }

        Optional<Coupon> couponOpt = couponRepository.findById(couponId);
        if (!couponOpt.isPresent()) {
            throw new BusinessException("优惠券不存在");
        }
        Coupon coupon = couponOpt.get();

        // 检查发放总量
        if (coupon.getTotalQuantity() != null && coupon.getTotalQuantity() <= 0) {
            throw new BusinessException("优惠券已发放完");
        }

        // 检查每人最多领取张数
        Integer count = userCouponRepository.countByUserIdAndCouponId(userId, couponId);
        if (coupon.getMaxPerUser() != null && count >= coupon.getMaxPerUser()) {
            throw new BusinessException("您已达到该优惠券的领取上限");
        }

        // 领取优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setReceivedAt(LocalDateTime.now());
        userCoupon.setStatus(0);

        userCouponRepository.save(userCoupon);

        // 更新发放总量
        if (coupon.getTotalQuantity() != null) {
            coupon.setTotalQuantity(coupon.getTotalQuantity() - 1);
            couponRepository.save(coupon);
        }

        return userCoupon;
    }

    @Override
    public List<CouponDTO> getCouponsInUserWallet(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        return userCoupons.stream()
                .map(userCoupon -> {
                    Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).orElse(null);
                    if (coupon == null) {
                        return null;
                    }
                    CouponDTO dto = new CouponDTO();
                    BeanUtils.copyProperties(coupon, dto);
                    dto.setStatus(userCoupon.getStatus());
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Coupon> getAvailableCoupons(Long userId, Integer packageId, BigDecimal orderPrice) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        List<Coupon> availableCoupons = new ArrayList<>();

        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(packageId);
        if (!packageOpt.isPresent()) {
            return availableCoupons;
        }
        GroupBuyPackage groupBuyPackage = packageOpt.get();

        for (UserCoupon userCoupon : userCoupons) {
            if (userCoupon.getStatus() == 0) {
                Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).orElse(null);
                if (coupon != null) {
                    // 检查使用门槛
                    if (coupon.getUseThreshold() != null && orderPrice.compareTo(coupon.getUseThreshold()) < 0) {
                        continue;
                    }
                    // 检查适用条件
                    if (coupon.getApplicableCategory() != null && !coupon.getApplicableCategory().equals(groupBuyPackage.getTitle())) {
                        continue;
                    }
                    if (coupon.getApplicableShop() != null && !coupon.getApplicableShop().equals(groupBuyPackage.getShopId())) {
                        continue;
                    }
                    // 检查有效期
                    if (coupon.getExpirationDate() != null && LocalDateTime.now().isAfter(coupon.getExpirationDate())) {
                        continue;
                    }
                    if (coupon.getValidDays() != null) {
                        LocalDateTime expiration = userCoupon.getReceivedAt().plusDays(coupon.getValidDays());
                        if (LocalDateTime.now().isAfter(expiration)) {
                            continue;
                        }
                    }
                    availableCoupons.add(coupon);
                }
            }
        }
        return availableCoupons;
    }

    @Override
    public Coupon getMaxDiscountCoupon(Long userId, Integer packageId, BigDecimal orderPrice) {
        List<Coupon> availableCoupons = getAvailableCoupons(userId, packageId, orderPrice);
        return availableCoupons.stream()
                .max(Comparator.comparing(coupon -> calculateDiscount(coupon, orderPrice)))
                .orElse(null);
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderPrice) {
        if ("减固定金额".equals(coupon.getType())) {
            return coupon.getAmount();
        } else if ("减到固定金额".equals(coupon.getType())) {
            return orderPrice.subtract(coupon.getAmount());
        } else if ("折扣券".equals(coupon.getType())) {
            BigDecimal discount = orderPrice.multiply(BigDecimal.ONE.subtract(coupon.getAmount()));
            if (coupon.getMaxDeduction() != null && discount.compareTo(coupon.getMaxDeduction()) > 0) {
                return coupon.getMaxDeduction();
            }
            return discount;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isNewUser(Long userId) {
        List<com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // 使用 isEmpty() 替代 size() == 0
        return orders.isEmpty();
    }

    @Override
    public boolean hasReceivedNewUserCoupon(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        for (UserCoupon userCoupon : userCoupons) {
            Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).orElse(null);
            if (coupon != null && coupon.isNewUserCoupon()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Coupon> getNewUserCoupons() {
        // 硬编码新人券配置
        List<Coupon> newUserCoupons = new ArrayList<>();

        // 新人KFC9折券
        Coupon kfcCoupon = new Coupon();
        kfcCoupon.setTitle("新人KFC9折券");
        kfcCoupon.setType("折扣券");
        kfcCoupon.setAmount(new BigDecimal("0.1")); // 折扣率为 10%（即 9 折）
        kfcCoupon.setUseThreshold(new BigDecimal("10"));
        kfcCoupon.setApplicableShop("KFC南区店");
        kfcCoupon.setValidDays(7);
        kfcCoupon.setTotalQuantity(10000);
        kfcCoupon.setMaxPerUser(1);
        // 正确调用 Lombok 生成的 setter 方法（布尔类型属性命名规范）
        kfcCoupon.setNewUserCoupon(true);
        newUserCoupons.add(kfcCoupon);

        // 新人奶茶免单券（减到固定金额 0 元，即免单）
        Coupon milkTeaCoupon = new Coupon();
        milkTeaCoupon.setTitle("新人奶茶免单券");
        milkTeaCoupon.setType("减到固定金额");
        milkTeaCoupon.setAmount(BigDecimal.ZERO);
        milkTeaCoupon.setMaxDeduction(new BigDecimal("15")); // 最大抵扣 15 元（实际免单时金额为 0，此参数可能冗余，按需保留）
        milkTeaCoupon.setUseThreshold(null); // 无使用门槛
        milkTeaCoupon.setApplicableCategory("奶茶");
        milkTeaCoupon.setValidDays(7);
        milkTeaCoupon.setTotalQuantity(100);
        milkTeaCoupon.setMaxPerUser(1);
        milkTeaCoupon.setNewUserCoupon(true);
        newUserCoupons.add(milkTeaCoupon);

        // 新人100元优惠券（满200减100）
        Coupon hundredCoupon = new Coupon();
        hundredCoupon.setTitle("新人100元优惠券");
        hundredCoupon.setType("减固定金额");
        hundredCoupon.setAmount(new BigDecimal("100"));
        hundredCoupon.setUseThreshold(new BigDecimal("200"));
        hundredCoupon.setApplicableCategory(null); // 适用所有品类
        hundredCoupon.setApplicableShop(null); // 适用所有店铺
        hundredCoupon.setValidDays(1); // 领取后 1 天内有效
        hundredCoupon.setTotalQuantity(1); // 仅发放 1 张
        hundredCoupon.setMaxPerUser(1);
        hundredCoupon.setNewUserCoupon(true);
        newUserCoupons.add(hundredCoupon);

        return newUserCoupons;
    }
}