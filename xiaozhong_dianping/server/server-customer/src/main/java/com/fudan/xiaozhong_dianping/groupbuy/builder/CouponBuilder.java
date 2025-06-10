package com.fudan.xiaozhong_dianping.groupbuy.builder;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * 优惠券建造者
 * 用于安全地构建优惠券对象，处理必填字段和可选字段
 */
public class CouponBuilder {

    // 必填字段
    private String title;
    private String type;
    private BigDecimal amount;

    // 可选字段
    private String description;
    private BigDecimal maxDeduction;
    private BigDecimal useThreshold;
    private String applicableCategory;
    private String applicableShop;
    private LocalDateTime expirationDate;
    private Integer validDays;
    private Integer totalQuantity;
    private Integer maxPerUser;
    private boolean isNewUserCoupon;
    private List<Integer> categoryIds = new ArrayList<>();

    /**
     * 创建建造者实例，初始化必填字段
     * @param title 优惠券标题
     * @param type 优惠券类型（减固定金额、减到固定金额、折扣券）
     * @param amount 优惠券金额或折扣率
     * @return 建造者实例
     */
    public static CouponBuilder builder(String title, String type, BigDecimal amount) {
        CouponBuilder builder = new CouponBuilder();
        builder.title = title;
        builder.type = type;
        builder.amount = amount;
        return builder;
    }

    /**
     * 设置优惠券描述
     * @param description 描述
     * @return 当前建造者实例
     */
    public CouponBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * 设置最大抵扣金额
     * @param maxDeduction 最大抵扣金额
     * @return 当前建造者实例
     */
    public CouponBuilder maxDeduction(BigDecimal maxDeduction) {
        this.maxDeduction = maxDeduction;
        return this;
    }

    /**
     * 设置使用门槛
     * @param useThreshold 使用门槛金额
     * @return 当前建造者实例
     */
    public CouponBuilder useThreshold(BigDecimal useThreshold) {
        this.useThreshold = useThreshold;
        return this;
    }

    /**
     * 设置适用品类
     * @param categoryIds 品类ID列表
     * @return 当前建造者实例
     */
    public CouponBuilder applicableCategories(List<Integer> categoryIds) {
        this.categoryIds = categoryIds;
        return this;
    }

    /**
     * 设置适用店铺
     * @param shopId 店铺ID
     * @return 当前建造者实例
     */
    public CouponBuilder applicableShop(String shopId) {
        this.applicableShop = shopId;
        return this;
    }

    /**
     * 设置过期时间
     * @param expirationDate 过期时间
     * @return 当前建造者实例
     */
    public CouponBuilder expirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    /**
     * 设置有效天数（从领取时开始计算）
     * @param validDays 有效天数
     * @return 当前建造者实例
     */
    public CouponBuilder validDays(Integer validDays) {
        this.validDays = validDays;
        return this;
    }

    /**
     * 设置发放总量
     * @param totalQuantity 发放总量
     * @return 当前建造者实例
     */
    public CouponBuilder totalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
        return this;
    }

    /**
     * 设置每人最多领取张数
     * @param maxPerUser 每人最多领取张数
     * @return 当前建造者实例
     */
    public CouponBuilder maxPerUser(Integer maxPerUser) {
        this.maxPerUser = maxPerUser;
        return this;
    }

    /**
     * 设置是否为新人券
     * @param isNewUserCoupon 是否为新人券
     * @return 当前建造者实例
     */
    public CouponBuilder isNewUserCoupon(boolean isNewUserCoupon) {
        this.isNewUserCoupon = isNewUserCoupon;
        return this;
    }

    /**
     * 根据当前设置构建特定类型的减固定金额优惠券
     * @param amount 减免金额
     * @return 当前建造者实例
     */
    public static CouponBuilder fixedAmount(String title, BigDecimal amount) {
        return builder(title, "减固定金额", amount);
    }

    /**
     * 根据当前设置构建特定类型的减至固定金额优惠券
     * @param targetAmount 目标金额
     * @return 当前建造者实例
     */
    public static CouponBuilder fixToAmount(String title, BigDecimal targetAmount) {
        return builder(title, "减到固定金额", targetAmount);
    }

    /**
     * 根据当前设置构建特定类型的折扣券
     * @param discountRate 折扣率（如0.9表示9折）
     * @return 当前建造者实例
     */
    public static CouponBuilder percentage(String title, BigDecimal discountRate) {
        // 验证折扣率是否合法：0-1之间
        if (discountRate.compareTo(BigDecimal.ZERO) <= 0 ||
                discountRate.compareTo(BigDecimal.ONE) >= 0) {
            throw new BusinessException("折扣率必须在0-1之间");
        }
        return builder(title, "折扣券", discountRate);
    }

    /**
     * 根据当前设置构建免单券
     * @return 当前建造者实例
     */
    public static CouponBuilder freeOrder(String title) {
        return builder(title, "减到固定金额", BigDecimal.ZERO);
    }

    /**
     * 根据当前设置构建优惠券对象
     * @return 构建完成的优惠券对象
     */
    public Coupon build() {
        // 校验必填字段
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("优惠券标题不能为空");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new BusinessException("优惠券类型不能为空");
        }
        if (amount == null) {
            throw new BusinessException("优惠券金额/折扣率不能为空");
        }

        // 创建优惠券实例
        Coupon coupon = new Coupon();
        coupon.setTitle(title);
        coupon.setType(type);
        coupon.setAmount(amount);
        coupon.setDescription(description);
        coupon.setMaxDeduction(maxDeduction);
        coupon.setUseThreshold(useThreshold);
        coupon.setApplicableShop(applicableShop);
        coupon.setExpirationDate(expirationDate);
        coupon.setValidDays(validDays);
        coupon.setTotalQuantity(totalQuantity);
        coupon.setMaxPerUser(maxPerUser);
        coupon.setNewUserCoupon(isNewUserCoupon);

        // 其他逻辑处理，如关联品类等
        // 注意：实际保存关联品类需要在服务层处理

        return coupon;
    }
}