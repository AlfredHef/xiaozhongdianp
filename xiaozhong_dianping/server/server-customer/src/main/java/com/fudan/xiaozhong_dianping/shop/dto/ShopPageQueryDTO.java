package com.fudan.xiaozhong_dianping.shop.dto;

import lombok.Data;

import java.io.Serializable;

@Data

public class ShopPageQueryDTO implements Serializable {
    private String name;
    private String category;
    private Integer offset;
    private Integer pageSize;
    private Long userId;


}
