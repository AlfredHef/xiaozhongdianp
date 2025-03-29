package com.fudan.xiaozhong_dianping.shop.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 店铺分页查询数据传输对象
 * 用于封装分页查询店铺信息时的参数条件
 */
@Data
public class ShopPageQueryDTO implements Serializable {
    private String name;        // 搜索关键词
    private String category;    // 分类名称
    private Integer pageCurrent;     // 当前页码
    private Integer pageSize;   // 每页大小
    private Integer offset;     // 分页偏移量
    private String sortBy;      // 排序字段
    private Long userId;

    /* 每页显示记录数 */


    // 新增筛选字段
    private Double minRating;    // 最低评分
    private Double maxRating;    // 最高评分
    private Double minPrice;     // 最低价格
    private Double maxPrice;     // 最高价格
    private Double minAverageCost; // 最低人均消费
    private Double maxAverageCost; // 最高人均消费

    public Long getUserId() {
        return userId;
    }
}
