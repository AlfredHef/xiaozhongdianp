package com.fudan.xiaozhong_dianping.shop.service.impl;

import com.fudan.result.PageResult;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.mapper.SearchHistoryMapper;
import com.fudan.xiaozhong_dianping.shop.mapper.ShopMapper;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * ShopServiceImpl类实现了ShopService接口，提供了一系列与商店相关的服务方法
 * 它使用了Spring的@Service注解，标志着它是一个服务层组件
 */
public class ShopServiceImpl implements ShopService {

    @Autowired
    /**
     * 自动注入ShopMapper接口的实现类，用于访问商店相关的数据库操作
     */
    private ShopMapper shopMapper;

    @Autowired
    /**
     * 自动注入SearchHistoryMapper接口的实现类，用于访问搜索历史相关的数据库操作
     */
    private SearchHistoryMapper searchHistoryMapper;

    @Override
    /**
     * 保存用户的搜索历史记录
     *
     * @param searchHistory 搜索历史对象，包含用户ID和搜索关键词等信息
     * @return 如果插入操作成功，则返回true；否则返回false
     */
    public Boolean saveSearchHistory(SearchHistory searchHistory) {
        return searchHistoryMapper.insert(searchHistory) > 0;
    }

    @Override
    /**
     * 根据查询条件搜索商店信息
     *
     * @param shopPageQueryDTO 包含分页和查询条件的DTO对象
     * @return 返回查询到的商店列表
     */
    public List<Shop> searchShops(ShopPageQueryDTO shopPageQueryDTO) {
        List<Shop> result = shopMapper.searchShops(shopPageQueryDTO);
        return result;
    }

    @Override
    /**
     * 根据用户ID获取该用户的搜索历史记录
     *
     * @param userId 用户ID，用于查询搜索历史
     * @return 返回该用户的搜索历史记录列表
     */
    public List<SearchHistory> getSearchHistoryByUserId(Long userId) {
        return searchHistoryMapper.getSearchHistoryByUserId(userId);
    }

    @Override
    public List<Shop> showShops(int pageCurrent, int pageSize) {
        List<Shop> list=shopMapper.showShops(pageCurrent,pageSize);
        return list;
    }


}
