package com.fudan.xiaozhong_dianping.shop.mapper;

import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShopImageMapper {
    /**
     * 根据商家ID查询图片列表
     * @param shopId 商家ID
     * @return 图片列表
     */
    List<ShopImage> findImagesByShopId(@Param("shopId") Long shopId);
}