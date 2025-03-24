package com.fudan.xiaozhong_dianping.shop.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 该类表示商家的分类信息，用于对商家进行分类管理，同时可作为搜索关键词匹配的依据。
 * 借助 Lombok 注解简化代码，自动生成 getter、setter、构造函数等常用方法。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    /**
     * 分类的唯一标识符，为自增的整数类型。
     * 此 ID 用于在系统中唯一标识不同的商家分类。
     */
    private Integer id;

    /**
     * 分类的名称，例如 "火锅"、"奶茶" 等。
     * 该名称具有唯一性约束，用于搜索关键词匹配以及展示分类信息。
     */
    private String name;
}