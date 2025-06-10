package com.fudan.xiaozhong_dianping.groupbuy.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
}