package com.fudan.xiaozhong_dianping.shop.mapper;

import com.fudan.result.PageResult;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import org.apache.ibatis.annotations.Mapper;

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
}
