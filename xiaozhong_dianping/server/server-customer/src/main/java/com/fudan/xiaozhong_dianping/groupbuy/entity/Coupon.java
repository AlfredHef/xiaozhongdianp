package com.fudan.xiaozhong_dianping.groupbuy.entity;

import com.fudan.xiaozhong_dianping.groupbuy.enums.CouponType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 优惠券实体类
 */
@Data
@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String type; // 减固定金额、减到固定金额、折扣券

    @Column
    private BigDecimal amount; // 固定金额或折扣率

    @Column
    private BigDecimal maxDeduction; // 最大抵扣金额

    @Column
    private BigDecimal useThreshold; // 使用门槛

    @Column
    private String applicableCategory; // 适用品类

    @Column
    private String applicableShop; // 适用店铺

    @Column
    private LocalDateTime expirationDate; // 截止日期

    @Column
    private Integer validDays; // 领取后有效天数

    @Column
    private Integer totalQuantity; // 发放总量

    @Column
    private Integer maxPerUser; // 每人最多领取张数

    @Column
    private boolean isNewUserCoupon; // 是否为新人券
    
    // 关联的品类
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL)
    private List<CouponCategory> categories;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取优惠券类型枚举
     */
    public CouponType getCouponType() {
        return CouponType.fromDisplayName(type);
    }

    /**
     * 获取优惠券的格式化描述
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        
        // 添加类型描述
        sb.append("类型: ").append(getCouponType().getFormattedDescription()).append("\n");
        
        // 添加优惠信息
        switch (getCouponType()) {
            case FIXED_AMOUNT:
                sb.append("优惠金额: ¥").append(amount).append("\n");
                break;
            case FIXED_PRICE:
                sb.append("优惠后价格: ¥").append(amount).append("\n");
                break;
            case DISCOUNT:
                sb.append("折扣率: ").append(amount.multiply(new BigDecimal("100"))).append("%\n");
                break;
            case THRESHOLD:
                sb.append("满").append(useThreshold).append("减").append(amount).append("\n");
                break;
        }
        
        // 添加使用门槛
        if (useThreshold != null) {
            sb.append("使用门槛: 满¥").append(useThreshold).append("\n");
        }
        
        // 添加最大抵扣金额
        if (maxDeduction != null) {
            sb.append("最大抵扣: ¥").append(maxDeduction).append("\n");
        }
        
        // 添加有效期
        if (expirationDate != null) {
            sb.append("有效期至: ").append(expirationDate.format(DATE_FORMATTER)).append("\n");
        } else if (validDays != null) {
            sb.append("领取后").append(validDays).append("天内有效\n");
        }
        
        // 添加使用限制
        if (maxPerUser != null) {
            sb.append("每人限领").append(maxPerUser).append("张\n");
        }
        
        // 添加适用范围
        if (applicableCategory != null) {
            sb.append("适用品类: ").append(applicableCategory).append("\n");
        }
        if (applicableShop != null) {
            sb.append("适用店铺: ").append(applicableShop).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * 获取优惠券的简短描述
     */
    public String getShortDescription() {
        StringBuilder sb = new StringBuilder();
        switch (getCouponType()) {
            case FIXED_AMOUNT:
                sb.append("减").append(amount).append("元");
                break;
            case FIXED_PRICE:
                sb.append("减至").append(amount).append("元");
                break;
            case DISCOUNT:
                sb.append(amount.multiply(new BigDecimal("100"))).append("折");
                break;
            case THRESHOLD:
                sb.append("满").append(useThreshold).append("减").append(amount);
                break;
        }
        return sb.toString();
    }

    /**
     * 计算优惠金额
     * @param originalPrice 原价
     * @return 优惠金额
     */
    public BigDecimal calculateDiscount(BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 检查使用门槛
        if (useThreshold != null && originalPrice.compareTo(useThreshold) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        switch (getCouponType()) {
            case FIXED_AMOUNT:
                discount = amount;
                break;
            case FIXED_PRICE:
                discount = originalPrice.subtract(amount);
                break;
            case DISCOUNT:
                // 折扣券的amount存储的是折扣率
                discount = originalPrice.multiply(BigDecimal.ONE.subtract(amount));
                break;
        }

        // 检查最大抵扣金额
        if (maxDeduction != null && discount.compareTo(maxDeduction) > 0) {
            discount = maxDeduction;
        }

        return discount.compareTo(BigDecimal.ZERO) > 0 ? discount : BigDecimal.ZERO;
    }

    /**
     * 检查优惠券是否可用
     * @param originalPrice 原价
     * @return 是否可用
     */
    public boolean isAvailable(BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 检查使用门槛
        if (useThreshold != null && originalPrice.compareTo(useThreshold) < 0) {
            return false;
        }

        // 检查是否过期
        if (expirationDate != null && LocalDateTime.now().isAfter(expirationDate)) {
            return false;
        }

        return true;
    }

    /**
     * 获取优惠券状态描述
     */
    public String getStatusDescription() {
        if (expirationDate != null && LocalDateTime.now().isAfter(expirationDate)) {
            return "已过期";
        }
        if (totalQuantity != null && totalQuantity <= 0) {
            return "已领完";
        }
        return "可领取";
    }
}