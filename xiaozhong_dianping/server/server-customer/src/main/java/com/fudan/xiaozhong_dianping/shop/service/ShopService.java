package com.fudan.xiaozhong_dianping.shop.service;

import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;

import java.util.List;

/**
 * ShopService接口定义了与商店搜索和管理相关的服务方法
 */
public interface ShopService {

    /**
     * 保存用户的搜索历史记录
     *
     * @param searchHistory 搜索历史记录对象，包含用户ID和搜索关键词等信息
     * @return 返回一个布尔值，表示搜索历史记录是否成功保存
     */
    Boolean saveSearchHistory(SearchHistory searchHistory);

    /**
     * 根据查询条件搜索商店列表
     *
     * @param shopPageQueryDTO 商店页面查询DTO对象，包含分页和查询条件信息
     * @return 返回一个商店实体列表，满足查询条件的商店信息
     */
    List<Shop> searchShops(ShopPageQueryDTO shopPageQueryDTO);

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
     * 批量导入商家及其图片数据
     *
     * @param shops 商家列表
     * @param imagePaths 图片路径列表，需与商家列表一一对应
     */
    void importShopsFromCSV(List<Shop> shops, List<String> imagePaths);

    /**
     * 批量导入商家及其图片数据（带描述）
     *
     * @param shops 商家列表
     * @param shopImages 图片信息列表，每个对象包含 shopId 和图片路径及描述
     */
    void importShopsFromCSVWithDescriptions(List<Shop> shops, List<ShopImage> shopImages);
}