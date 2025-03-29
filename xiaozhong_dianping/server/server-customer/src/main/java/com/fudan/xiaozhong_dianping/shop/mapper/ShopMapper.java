package com.fudan.xiaozhong_dianping.shop.mapper;

import com.fudan.result.PageResult;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopMapper {


    /**
     * 根据查询条件搜索商家列表
     * @param shopPageQueryDTO 包含分页和搜索条件的查询对象
     * @return 商家列表
     */
    List<Shop> searchShops(ShopPageQueryDTO shopPageQueryDTO);

    /**
     * 统计符合查询条件的商家数量
     * @param shopPageQueryDTO 包含分页和搜索条件的查询对象
     * @return 符合条件的商家数量
     */
    int countShops(ShopPageQueryDTO shopPageQueryDTO);


    /**
     * 分页查询店铺列表
     *
     * @param offset 分页偏移量
     * @param pageSize 每页显示的记录数量，需大于0
     * @return 包含分页结果的店铺列表，当无数据时返回空列表（非null）
     */
    List<Shop> showShops(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据商家ID查询详情
     * @param id 商家ID
     * @return 商家实体
     */
    Shop findShopById(@Param("id") Long id);
//    当用户在前端分页列表中点击某个商家时，前端会获取该商家的 ID，然后向后端发送一个请求，
//    请求该商家的详细信息。后端接收到这个 ID 后，
//    调用 findShopById 方法从数据库中查询对应的商家数据，
//    再返回给前端，前端拿到数据后渲染详情页。

    /**
     * 获取商家总数
     * @return 商家总数
     */
    int countAllShops();

}
