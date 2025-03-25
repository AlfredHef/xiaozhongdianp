package com.fudan.xiaozhong_dianping.shop.dto;

import lombok.Data;

import java.io.Serializable;

@Data

public class ShopPageQueryDTO implements Serializable {
    private String name;        // 搜索关键词
    private String category;    // 分类名称
    private Integer offset;     // 分页偏移量
    private Integer pageSize;   // 分页大小
    private String sortBy;      // 新增排序字段，例如："rating_desc"（评分降序）、"average_cost_asc"（人均消费升序）
    private Long userId;
}
