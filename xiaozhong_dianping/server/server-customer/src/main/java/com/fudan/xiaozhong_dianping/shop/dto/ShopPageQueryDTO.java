package com.fudan.xiaozhong_dianping.shop.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 店铺分页查询数据传输对象
 * 用于封装分页查询店铺信息时的参数条件
 */
@Data
public class ShopPageQueryDTO implements Serializable {
    /* 店铺名称（支持模糊查询） */
    private String name;

    /* 店铺分类（精确匹配） */
    private String category;

    /* 分页偏移量（从第几条开始查询） */
    private Integer offset;

    /* 每页显示记录数 */
    private Integer pageSize;

    /* 用户ID（用于关联用户与店铺） */
    private Long userId;

    public int getPageCurrent() {
        return 0;
    }
}
