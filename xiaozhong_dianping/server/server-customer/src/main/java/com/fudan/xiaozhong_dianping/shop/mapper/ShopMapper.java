package com.fudan.xiaozhong_dianping.shop.mapper;

import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShopMapper {


    /**
     * 分页查询店铺列表
     *
     * @param offset 起始位置
     * @param limit 每页记录数
     * @return 分页后的店铺列表
     */
    @Select("SELECT * FROM shop LIMIT #{limit} OFFSET #{offset}")
    List<Shop> showShops(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 批量插入商家数据
     *
     * @param shops 商家列表
     */
    @Insert({
            "<script>",
            "INSERT INTO shop (name, address, other_columns) VALUES ",
            "<foreach collection='list' item='shop' separator=','>",
            "(#{shop.name}, #{shop.address}, #{shop.otherColumns})",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void batchInsertShops(@Param("list") List<Shop> shops);

    /**
     * 批量插入商家图片数据
     *
     * @param shopImages 商家图片列表
     */
    @Insert({
            "<script>",
            "INSERT INTO shop_image (shop_id, image_url, description) VALUES ",
            "<foreach collection='list' item='image' separator=','>",
            "(#{image.shopId}, #{image.imageUrl}, #{image.description})",
            "</foreach>",
            "</script>"
    })
    void batchInsertShopImages(@Param("list") List<ShopImage> shopImages);
}
