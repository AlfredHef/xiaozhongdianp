package com.fudan.xiaozhong_dianping.groupbuy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品实体类
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "packageDishRelations") // 排除关联列表以避免循环引用
@Entity
@Table(name = "dish")
public class Dish {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "shop_id", nullable = false)
    private Integer shopId;
    
    @Column(nullable = false)
    private String name;
    
    private BigDecimal price;
    
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联套餐菜品关系
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PackageDishRelation> packageDishRelations;
} 