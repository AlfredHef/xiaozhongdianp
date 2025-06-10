package com.fudan.xiaozhong_dianping.groupbuy.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 套餐菜品项数据传输对象
 */
@Data
public class PackageDishItemDTO {
    
    private Integer dishId;
    private String dishName;
    private BigDecimal dishPrice;
    private String dishDescription;
    private Integer quantity;
} 