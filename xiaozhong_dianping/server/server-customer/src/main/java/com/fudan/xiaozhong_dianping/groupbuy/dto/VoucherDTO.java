package com.fudan.xiaozhong_dianping.groupbuy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 券码数据传输对象
 */
@Data
public class VoucherDTO {
    
    private Long id;
    private Long orderId;
    private String code; // 16位券码
    private String qrCodeUrl; // 二维码URL
    private Integer status; // 0-未使用，1-已使用
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;
    
    // 订单相关信息
    private BigDecimal orderPrice;
    private LocalDateTime createdTime; // 下单时间
    
    // 套餐相关信息
    private Integer packageId;
    private String packageTitle;
    
    // 商家相关信息
    private Integer shopId;
    private String shopName;
} 