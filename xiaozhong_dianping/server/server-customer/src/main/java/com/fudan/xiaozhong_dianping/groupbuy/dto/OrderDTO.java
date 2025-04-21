package com.fudan.xiaozhong_dianping.groupbuy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单数据传输对象
 */
@Data
public class OrderDTO {
    
    private Long id;
    private Long userId;
    private Integer packageId;
    private Integer shopId;
    private BigDecimal orderPrice;
    private Integer status; // 0-已取消，1-已购买，2-已使用
    private LocalDateTime createdAt;
    
    // 套餐标题，展示用
    private String packageTitle;
    // 商家名称，展示用
    private String shopName;
} 