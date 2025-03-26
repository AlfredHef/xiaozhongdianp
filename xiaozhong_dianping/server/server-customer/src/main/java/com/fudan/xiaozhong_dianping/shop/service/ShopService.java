package com.fudan.xiaozhong_dianping.shop.service;

import com.fudan.result.PageResult;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;

import java.util.List;
import java.util.Map;

/**
 * ShopService接口定义了与商店搜索相关的服务方法
 */
public interface ShopService {

    /**
     * 保存用户的搜索历史记录
     *
     * @param searchHistory 搜索历史记录对象，包含用户搜索的相关信息
     * @return 返回一个布尔值，表示搜索历史记录是否成功保存
     */
    Boolean saveSearchHistory(SearchHistory searchHistory);

    /**
     * 根据查询条件搜索商店列表
     *
     * @param shopPageQueryDTO 商店页面查询DTO对象，包含分页和查询条件信息
     * @return 返回一个商店实体 列表，满足查询条件的商店信息
     */
    List<com.fudan.xiaozhong_dianping.shop.entity.Shop> searchShops(com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO shopPageQueryDTO);

    /**
     * 根据用户ID获取该用户的搜索历史记录
     *
     * @param userId 用户ID，用于标识特定的用户
     * @return 返回一个搜索历史记录列表，包含该用户的所有搜索历史
     */
    List<SearchHistory> getSearchHistoryByUserId(Long userId);

    /**
     * 分页查询店铺列表
     *
     * @param pageCurrent 当前页码，从1开始计数
     * @param pageSize 每页显示的记录数量
     * @return 包含分页数据的店铺列表，列表元素为Shop对象
     *         当无数据时返回空列表（非null）
     */
    List<Shop> showShops(int pageCurrent, int pageSize);

    /**
     * 获取商家详情（包括基本信息和图片）
     * @param shopId 商家ID
     * @return 包含商家详情和图片的Map，键分别为"shop"和"images"
     */
    Map<String, Object> getShopDetails(Long shopId);

}

