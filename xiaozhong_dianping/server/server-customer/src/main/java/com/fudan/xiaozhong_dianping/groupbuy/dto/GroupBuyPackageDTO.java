package com.fudan.xiaozhong_dianping.groupbuy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 团购套餐数据传输对象
 */
@Data
public class GroupBuyPackageDTO {
    
    private Integer id;
    private Integer shopId;
    private String title;
    private BigDecimal price;
    private String description;
    private Integer sales;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 套餐包含的菜品信息
    private List<PackageDishItemDTO> dishItems;
} 