package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.builder.CouponBuilder;
import com.fudan.xiaozhong_dianping.groupbuy.dto.CouponDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.entity.CouponCategory;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.Shop;
import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.repository.CouponCategoryRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.CouponRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyOrderRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.ShopRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.UserCouponRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import com.fudan.xiaozhong_dianping.shop.entity.Category;
import com.fudan.xiaozhong_dianping.shop.repository.CategoryRepository;
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
        return receiveCoupon(userId, couponId, false);
    }
    
    /**
     * 用户领取优惠券（带跳过新人券检查选项）
     *
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @param skipNewUserCheck 是否跳过新人券检查
     * @return 领取的用户优惠券信息
     * @throws BusinessException 当用户已领取过新人券、优惠券不存在、优惠券已发完或达到领取上限时抛出
     */
    @Override
    public UserCoupon receiveCoupon(Long userId, Long couponId, boolean skipNewUserCheck) {
        try {
            System.out.println("===== 开始领取优惠券 =====");
            System.out.println("用户ID: " + userId + ", 优惠券ID: " + couponId);
            
            // 新人券领取检查（可以跳过）
            if (!skipNewUserCheck && isNewUser(userId)) {
                boolean hasReceived = hasReceivedNewUserCoupon(userId);
                System.out.println("用户是新用户，是否已领取过新人券: " + hasReceived);
                
                if (hasReceived) {
                    throw new BusinessException("您已领取过新人券，不能再领取");
                }
            }

            // 检查优惠券是否存在
            System.out.println("尝试查询优惠券，ID: " + couponId);
            
            // 先判断ID是否为null
            if (couponId == null) {
                throw new BusinessException("优惠券ID不能为空");
            }
            
            Optional<Coupon> couponOpt = couponRepository.findById(couponId);
            System.out.println("优惠券查询结果: " + (couponOpt.isPresent() ? "存在" : "不存在"));
            
            if (!couponOpt.isPresent()) {
                // 检查数据库中是否有其他优惠券
                List<Coupon> allCoupons = couponRepository.findAll();
                System.out.println("数据库中所有优惠券数量: " + allCoupons.size());
                if (!allCoupons.isEmpty()) {
                    System.out.println("第一张优惠券ID: " + allCoupons.get(0).getId());
                }
                
                throw new BusinessException("优惠券不存在，ID: " + couponId);
            }
            Coupon coupon = couponOpt.get();
            System.out.println("查询到优惠券: ID=" + coupon.getId() + ", 标题=" + coupon.getTitle());

            // 检查发放总量
            if (coupon.getTotalQuantity() != null && coupon.getTotalQuantity() <= 0) {
                throw new BusinessException("优惠券已发放完");
            }

            // 检查每人最多领取张数
            Integer count = userCouponRepository.countByUserIdAndCouponId(userId, couponId);
            System.out.println("用户已领取该优惠券数量: " + count);
            
            if (coupon.getMaxPerUser() != null && count >= coupon.getMaxPerUser()) {
                throw new BusinessException("您已达到该优惠券的领取上限");
            }

            // 领取优惠券
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setReceivedAt(LocalDateTime.now());
            userCoupon.setStatus(0);

            // 保存到数据库
            System.out.println("准备保存用户优惠券关联记录...");
            UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);
            System.out.println("用户优惠券关联记录已保存，ID: " + savedUserCoupon.getId());

            // 更新发放总量
            if (coupon.getTotalQuantity() != null) {
                coupon.setTotalQuantity(coupon.getTotalQuantity() - 1);
                couponRepository.save(coupon);
                System.out.println("优惠券剩余数量已更新，剩余: " + coupon.getTotalQuantity());
            }

            return savedUserCoupon;
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
                    dto.setReceivedAt(userCoupon.getReceivedAt()); // 添加领取时间
                    dto.setDiscountAmount(coupon.getAmount()); // 设置折扣金额
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
                .filter(coupon -> {
                    // 检查优惠券是否过期
                    if (coupon.getExpirationDate() != null && 
                        coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
                        return false;
                    }

                    // 检查使用门槛
                    if (coupon.getUseThreshold() != null && 
                        orderPrice.compareTo(coupon.getUseThreshold()) < 0) {
                        return false;
                    }

                    // 检查适用店铺
                    if (coupon.getApplicableShop() != null) {
                        // 尝试将applicableShop解析为店铺ID
                        try {
                            Integer shopId = Integer.parseInt(coupon.getApplicableShop());
                            if (!shopId.equals(shop.getId())) {
                                System.out.println("优惠券ID:" + coupon.getId() + " 不适用于店铺ID:" + shop.getId() + "，优惠券适用店铺ID:" + shopId);
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // 如果不是数字，则认为是店铺名称，与店铺名称比较
                            if (!coupon.getApplicableShop().equals(shop.getName())) {
                                System.out.println("优惠券ID:" + coupon.getId() + " 不适用于店铺名称:" + shop.getName() + "，优惠券适用店铺名称:" + coupon.getApplicableShop());
                                return false;
                            }
                        }
                    }

                    return true;
                })
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
        return availableCoupons.stream()
                .max(Comparator.comparing(coupon -> calculateDiscount(coupon, orderPrice)))
                .orElse(null);
    }

    /**
     * 计算优惠券折扣金额
     *
     * @param coupon 优惠券
     * @param orderPrice 订单价格
     * @return 折扣金额
     */
    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderPrice) {
        if ("减固定金额".equals(coupon.getType())) {
            return coupon.getAmount();
        } else if ("减到固定金额".equals(coupon.getType())) {
            // 如果优惠后金额为0，则为免单券，最大优惠不超过原价
            if (coupon.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                // 如果有最大抵扣金额限制，则取较小值
                if (coupon.getMaxDeduction() != null && orderPrice.compareTo(coupon.getMaxDeduction()) > 0) {
                    return coupon.getMaxDeduction();
                }
                return orderPrice; // 全额抵扣
            }
            // 常规减到固定金额，如果固定金额小于订单金额，则计算折扣差额
            if (coupon.getAmount().compareTo(orderPrice) < 0) {
                return orderPrice.subtract(coupon.getAmount());
            }
            return BigDecimal.ZERO; // 如果固定金额大于订单金额，则无法使用
        } else if ("折扣券".equals(coupon.getType())) {
            // 折扣券的amount存储的是折扣率，如8折券存储的是0.8
            // 折扣金额 = 订单金额 * (1 - 折扣率)
            BigDecimal discount = orderPrice.multiply(BigDecimal.ONE.subtract(coupon.getAmount()));
            if (coupon.getMaxDeduction() != null && discount.compareTo(coupon.getMaxDeduction()) > 0) {
                return coupon.getMaxDeduction();
            }
            return discount;
        } else if ("秒杀券".equals(coupon.getType())) {
            // 秒杀券：如果订单金额大于maxDeduction，则减(maxDeduction - amount)
            // 如果订单金额小于maxDeduction，则减到amount
            if (orderPrice.compareTo(coupon.getMaxDeduction()) > 0) {
                return orderPrice.subtract(coupon.getAmount());
            } else {
                return orderPrice.subtract(coupon.getAmount());
            }
        } else if ("免单券".equals(coupon.getType())) {
            // 免单券：如果订单金额大于maxDeduction，则减maxDeduction
            // 如果订单金额小于maxDeduction，则全额减免
            if (coupon.getMaxDeduction() != null && orderPrice.compareTo(coupon.getMaxDeduction()) > 0) {
                return coupon.getMaxDeduction();
            } else {
                return orderPrice;
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean hasReceivedReviewReward(Long userId) {
        // 查询用户是否拥有点评奖励券（通过标题识别）
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        return userCoupons.stream()
                .map(uc -> couponRepository.findById(uc.getCouponId()).orElse(null))
                .anyMatch(c -> c != null && c.getTitle() != null && c.getTitle().contains("点评奖励"));
    }

    @Override
    public void grantReviewRewardCoupon(Long userId) {
        // 构建奖励券（8折，最高抵扣20元，7天有效）
        Coupon rewardCoupon = CouponBuilder.builder("点评奖励8折券", "折扣券", new BigDecimal("0.8"))
                .description("任意品类通用，最高抵扣20元，7天有效")
                .maxDeduction(new BigDecimal("20"))
                .validDays(7)
                .totalQuantity(1000) // 总库存
                .maxPerUser(1) // 每人限领1张
                .build();

        // 保存优惠券并发放（跳过新人券检查）
        Coupon savedCoupon = couponRepository.save(rewardCoupon);
        receiveCoupon(userId, savedCoupon.getId(), true);
    }

    @Override
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
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
                    if (coupon == null) {
                        return null;
                    }
                    
                    // 检查有效期
                    if (coupon.getExpirationDate() != null && LocalDateTime.now().isAfter(coupon.getExpirationDate())) {
                        return null;
                    }
                    if (coupon.getValidDays() != null) {
                        LocalDateTime expiration = userCoupon.getReceivedAt().plusDays(coupon.getValidDays());
                        if (LocalDateTime.now().isAfter(expiration)) {
                            return null;
                        }
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

    /**
     * 优惠券过期检查的定时任务方法
     * 每天凌晨0点检查所有未使用的优惠券是否过期
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行一次
    public void checkCouponExpiration() {
        // 获取所有未使用的用户优惠券
        List<UserCoupon> activeCoupons = userCouponRepository.findByStatus(0);
        LocalDateTime now = LocalDateTime.now();
        
        for (UserCoupon userCoupon : activeCoupons) {
            Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).orElse(null);
            if (coupon != null) {
                boolean isExpired = false;
                
                // 检查固定过期时间
                if (coupon.getExpirationDate() != null && now.isAfter(coupon.getExpirationDate())) {
                    isExpired = true;
                } 
                // 检查领取后有效天数
                else if (coupon.getValidDays() != null) {
                    LocalDateTime expiration = userCoupon.getReceivedAt().plusDays(coupon.getValidDays());
                    if (now.isAfter(expiration)) {
                        isExpired = true;
                    }
                }
                
                // 如果已过期，更新状态
                if (isExpired) {
                    userCoupon.setStatus(2); // 设置为已过期
                    userCouponRepository.save(userCoupon);
                }
            }
        }
    }
}
