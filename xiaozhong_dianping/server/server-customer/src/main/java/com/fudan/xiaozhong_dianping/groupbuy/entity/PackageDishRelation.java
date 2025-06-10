package com.fudan.xiaozhong_dianping.groupbuy.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.persistence.*;

/**
 * 套餐菜品关联实体类
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"groupBuyPackage", "dish"}) // 排除关联对象以避免循环引用
@Entity
@Table(name = "package_dish_relation")
public class PackageDishRelation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private GroupBuyPackage groupBuyPackage;
    
    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;
    
    @Column(nullable = false)
    private Integer quantity;
} 