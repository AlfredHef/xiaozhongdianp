package com.fudan.xiaozhong_dianping.groupbuy.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 优惠券-品类关联实体类
 */
@Data
@Entity
@Table(name = "coupon_category")
public class CouponCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long couponId;
    
    @Column(nullable = false)
    private Integer categoryId;
    
    // 关联优惠券
    @ManyToOne
    @JoinColumn(name = "couponId", insertable = false, updatable = false)
    private Coupon coupon;
} 