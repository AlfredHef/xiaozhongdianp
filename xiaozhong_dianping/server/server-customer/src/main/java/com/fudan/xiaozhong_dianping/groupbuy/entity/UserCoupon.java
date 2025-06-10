package com.fudan.xiaozhong_dianping.groupbuy.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户优惠券关联实体类
 */
@Data
@Entity
@Table(name = "user_coupon")
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long couponId;

    @Column
    private LocalDateTime receivedAt;

    @Column
    private LocalDateTime usedAt;

    @Column
    private Integer status; // 0-未使用，1-已使用，2-已过期

    // 关联优惠券
    @ManyToOne
    @JoinColumn(name = "couponId", insertable = false, updatable = false)
    private Coupon coupon;
}