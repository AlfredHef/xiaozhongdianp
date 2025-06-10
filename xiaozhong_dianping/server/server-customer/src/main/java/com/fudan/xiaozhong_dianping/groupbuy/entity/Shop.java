package com.fudan.xiaozhong_dianping.groupbuy.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商家实体类
 */
@Data
@Entity
@Table(name = "shop")
public class Shop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String address;
    
    @Column
    private String businessHours;
    
    @Column
    private String phone;
    
    @Column
    private String description;
    
    @Column
    private BigDecimal averageCost;
    
    @Column
    private BigDecimal rating;
    
    @Column
    private BigDecimal priceMin;
    
    @Column
    private BigDecimal priceMax;
    
    @Column
    private Integer categoryId;
} 