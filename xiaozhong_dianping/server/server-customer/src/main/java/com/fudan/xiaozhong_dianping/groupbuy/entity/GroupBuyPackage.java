package com.fudan.xiaozhong_dianping.groupbuy.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 团购套餐实体类
 */
@Data
@Entity
@Table(name = "group_buying_package")
public class GroupBuyPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "shop_id", nullable = false)
    private Integer shopId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    private String description;
    
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer sales;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联套餐菜品关系
    @OneToMany(mappedBy = "groupBuyPackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PackageDishRelation> packageDishRelations;
    
    // 添加销量
    public void increaseSales() {
        if (this.sales == null) {
            this.sales = 0;
        }
        this.sales += 1;
    }
    
    // 初始化创建时间
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.sales == null) {
            this.sales = 0;
        }
    }
    
    // 更新时间
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 