package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.dto.CouponDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.CouponCategory;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Shop;
import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.factory.CouponFactory;
import com.fudan.xiaozhong_dianping.groupbuy.repository.CouponCategoryRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.CouponRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyOrderRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.ShopRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.UserCouponRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import com.fudan.xiaozhong_dianping.shop.entity.Category;
import com.fudan.xiaozhong_dianping.shop.repository.CategoryRepository;
import com.fudan.xiaozhong_dianping.groupbuy.validator.CouponValidator;
import com.fudan.xiaozhong_dianping.groupbuy.validator.CouponValidatorFactory;
import com.fudan.xiaozhong_dianping.groupbuy.validator.ValidationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Autowired
    private CouponCategoryRepository couponCategoryRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CouponValidatorFactory validatorFactory;

    /**
     * 创建新人优惠券
     */
    private Coupon createNewUserCoupon() {
        return CouponFactory.createNewUserCoupon(
            "新人专享券",
            new BigDecimal("20")
        );
    }

    /**
     * 创建满减优惠券
     */
    private Coupon createThresholdCoupon() {
        return CouponFactory.createThresholdCoupon(
            "满100减10",
            new BigDecimal("100"),
            new BigDecimal("10")
        );
    }

    /**
     * 创建折扣券
     */
    private Coupon createDiscountCoupon() {
        return CouponFactory.createDiscountCoupon(
            "9折优惠",
            new BigDecimal("0.9")
        );
    }

    /**
     * 用户领取优惠券
     *
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 领取的用户优惠券信息
     * @throws BusinessException 当用户已领取过新人券、优惠券不存在、优惠券已发完或达到领取上限时抛出
     */
    @Override
    public UserCoupon receiveCoupon(Long userId, Long couponId) {
        try {
            // 验证用户是否为新用户
            if (!isNewUser(userId)) {
                throw new BusinessException("只有新用户才能领取新人券");
            }

            // 验证是否已领取过新人券
            if (hasReceivedNewUserCoupon(userId)) {
                throw new BusinessException("您已领取过新人券");
            }

            // 获取优惠券
            Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException("优惠券不存在"));

            // 验证优惠券是否为新用户券
            if (!coupon.isNewUserCoupon()) {
                throw new BusinessException("该优惠券不是新人券");
            }

            // 创建用户优惠券关联
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setReceivedAt(LocalDateTime.now());
            userCoupon.setStatus(0); // 未使用状态

            return userCouponRepository.save(userCoupon);
        } catch (Exception e) {
            System.out.println("===== 领取优惠券异常 =====");
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
     * 获取用户钱包中的优惠券
     *
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
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

    /**
     * 获取用户可用的优惠券列表
     *
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param orderPrice 订单价格
     * @return 可用优惠券列表
     */
    @Override
    public List<Coupon> getAvailableCoupons(Long userId, Integer packageId, BigDecimal orderPrice) {
        // 获取用户所有未使用的优惠券
        List<UserCoupon> userCoupons = userCouponRepository.findByUserIdAndStatus(userId, 0);
        if (userCoupons.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取套餐所属的商家
        GroupBuyPackage groupBuyPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException("套餐不存在"));
        Shop shop = shopRepository.findById(groupBuyPackage.getShopId())
                .orElseThrow(() -> new BusinessException("商家不存在"));

        // 过滤出可用的优惠券
        return userCoupons.stream()
                .map(UserCoupon::getCoupon)
                .filter(coupon -> coupon.isAvailable(orderPrice))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户可用的最大折扣优惠券
     *
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param orderPrice 订单价格
     * @return 最大折扣优惠券
     */
    @Override
    public Coupon getMaxDiscountCoupon(Long userId, Integer packageId, BigDecimal orderPrice) {
        List<Coupon> availableCoupons = getAvailableCoupons(userId, packageId, orderPrice);
        if (availableCoupons.isEmpty()) {
            return null;
        }

        return availableCoupons.stream()
                .max(Comparator.comparing(coupon -> coupon.calculateDiscount(orderPrice)))
                .orElse(null);
    }

    /**
     * 检查用户是否为新人
     *
     * @param userId 用户ID
     * @return 如果用户为新人返回true，否则返回false
     */
    @Override
    public boolean isNewUser(Long userId) {
        List<com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.isEmpty();
    }

    /**
     * 检查用户是否已领取新人券
     *
     * @param userId 用户ID
     * @return 如果用户已领取新人券返回true，否则返回false
     */
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

    /**
     * 获取新人优惠券列表
     *
     * @return 新人优惠券列表
     */
    @Override
    public List<Coupon> getNewUserCoupons() {
        // 从数据库中查询所有标记为新人券的优惠券
        List<Coupon> newUserCoupons = couponRepository.findAllByIsNewUserCouponTrue();
        
        // 如果数据库中没有新人券数据，则返回空列表
        if (newUserCoupons == null || newUserCoupons.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 日志输出以便调试
        for (Coupon coupon : newUserCoupons) {
            System.out.println("从数据库获取到新人券: ID=" + coupon.getId() + ", 标题=" + coupon.getTitle());
        }
        
        return newUserCoupons;
    }

    /**
     * 获取用户所有可用的优惠券
     *
     * @param userId 用户ID
     * @return 用户可用的优惠券DTO列表
     */
    @Override
    public List<CouponDTO> getUserAvailableCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        return userCoupons.stream()
                .filter(userCoupon -> userCoupon.getStatus() == 0) // 只返回未使用的优惠券
                .map(userCoupon -> {
                    Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).orElse(null);
                    if (coupon == null || isExpired(coupon, userCoupon.getReceivedAt())) {
                        return null;
                    }
                    
                    CouponDTO dto = new CouponDTO();
                    BeanUtils.copyProperties(coupon, dto);
                    dto.setStatus(userCoupon.getStatus());
                    dto.setDiscountAmount(coupon.getAmount()); // 设置折扣金额
                    dto.setReceivedAt(userCoupon.getReceivedAt()); // 添加领取时间
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    private boolean isExpired(Coupon coupon, LocalDateTime receivedAt) {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查固定过期时间
        if (coupon.getExpirationDate() != null && now.isAfter(coupon.getExpirationDate())) {
            return true;
        }
        
        // 检查领取后有效天数
        if (coupon.getValidDays() != null) {
            LocalDateTime expiration = receivedAt.plusDays(coupon.getValidDays());
            return now.isAfter(expiration);
        }
        
        return false;
    }

    /**
     * 优惠券过期检查的定时任务方法
     * 每天凌晨0点检查所有未使用的优惠券是否过期
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行一次
    public void checkCouponExpiration() {
        userCouponRepository.findByStatus(0).stream()
                .filter(userCoupon -> {
                    Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).orElse(null);
                    return coupon != null && isExpired(coupon, userCoupon.getReceivedAt());
                })
                .forEach(userCoupon -> {
                    userCoupon.setStatus(2); // 设置为已过期
                    userCouponRepository.save(userCoupon);
                });
    }

    /**
     * 校验优惠券是否可用
     * @param coupon 优惠券
     * @param order 订单
     * @return 校验结果
     */
    public ValidationResult validateCoupon(Coupon coupon, Order order) {
        // 获取默认的校验器链
        CouponValidator validator = validatorFactory.createDefaultValidatorChain();
        
        // 执行校验
        return validator.validate(coupon, order);
    }
}
