package com.fudan.xiaozhong_dianping.groupbuy.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购订单实体类
 */
@Data
@Entity
@Table(name = "group_buy_order")
public class GroupBuyOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "package_id", nullable = false)
    private Integer packageId;
    
    @Column(name = "shop_id", nullable = false)
    private Integer shopId;
    
    @Column(name = "order_price", nullable = false)
    private BigDecimal orderPrice;
    
    @Column(name = "status", columnDefinition = "TINYINT DEFAULT 1")
    private Integer status; // 0-已取消，1-已购买，2-已使用
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 关联对应的套餐
    @ManyToOne
    @JoinColumn(name = "package_id", insertable = false, updatable = false)
    private GroupBuyPackage groupBuyPackage;
    
    // 一对一关联券码
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private VoucherCode voucherCode;
} 