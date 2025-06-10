package com.fudan.xiaozhong_dianping.groupbuy.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 券码实体类
 */
@Data
@Entity
@Table(name = "voucher_code")
public class VoucherCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(nullable = false, length = 16)
    private String code;
    
    @Column(name = "qr_code_url")
    private String qrCodeUrl;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status; // 0-未使用，1-已使用
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    // 关联的订单
    @OneToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private GroupBuyOrder order;
    
    // 生成16位随机数字券码
    public void generateCode() {
        StringBuilder sb = new StringBuilder(16);
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        this.code = sb.toString();
    }
    
    // 标记券码为已使用
    public void markAsUsed() {
        this.status = 1;
        this.usedAt = LocalDateTime.now();
    }
    
    // 初始化字段
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = 0;
        }
        if (this.code == null || this.code.isEmpty()) {
            generateCode();
        }
    }
} 