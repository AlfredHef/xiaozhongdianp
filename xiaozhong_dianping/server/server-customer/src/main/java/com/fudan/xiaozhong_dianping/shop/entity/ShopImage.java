package com.fudan.xiaozhong_dianping.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ShopImage 类用于表示与商家相关的图片信息，支持商家多图展示。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopImage {
    /**
     * 图片的唯一标识符，在系统中唯一标识一张商家图片。
     * 通常为自增的整数类型，作为数据库表的主键使用。
     */
    private Long id;

    /**
     * 该图片所属的商家ID，通过关联 Shop 实体类的 id，
     * 可以明确此图片是属于哪个具体的商家。
     */
    private Long shopId;

    /**
     * 图片的存储 URL，用于定位图片在存储系统中的位置。
     * 支持存储多张图片，方便商家展示不同角度或类型的图片。
     */
    private String imageUrl;

    /**
     * 图片的描述信息，例如 "招牌菜品"、"门店外观" 等。
     * 能够帮助用户快速了解图片所展示的内容。
     */
    private String description;
}