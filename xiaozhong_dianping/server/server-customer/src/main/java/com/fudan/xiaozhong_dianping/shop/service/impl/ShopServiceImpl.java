package com.fudan.xiaozhong_dianping.shop.service.impl;

import com.fudan.result.PageResult;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;
import com.fudan.xiaozhong_dianping.shop.mapper.SearchHistoryMapper;
import com.fudan.xiaozhong_dianping.shop.mapper.ShopImageMapper;
import com.fudan.xiaozhong_dianping.shop.mapper.ShopMapper;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ShopImageMapper shopImageMapper; // 新增注入图片Mapper
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

    /**
     * 分页查询店铺信息列表
     *
     * @param offset 分页偏移量
     * @param pageSize    每页显示的记录数量
     * @return            分页查询后的店铺信息集合，包含当前页的店铺数据
     */
    @Override
    public List<Shop> showShops(int offset, int pageSize) {
        // 调用数据访问层获取分页数据
        List<Shop> list = shopMapper.showShops(offset, pageSize);
        return list;
    }

    @Override
    public Map<String, Object> getShopDetails(Long shopId) {
        Map<String, Object> result = new HashMap<>();

        // 查询商家基本信息
        Shop shop = shopMapper.findShopById(shopId);
        result.put("shop", shop);

        // 查询商家图片（需确保ShopImageMapper已定义findImagesByShopId方法）
        List<ShopImage> images = shopImageMapper.findImagesByShopId(shopId);
        images.forEach(img -> {
            img.setImageUrl("/static/" + img.getImageUrl()); // 添加前缀
        });
        result.put("images", images);

        return result;
    }

    @Override
    public Boolean deleteSearchHistory(Long userId, Long historyId) {
        return searchHistoryMapper.deleteById(historyId) > 0;
    }

    @Override
    public Boolean clearSearchHistory(Long userId) {
        return searchHistoryMapper.deleteByUserId(userId) > 0;
    }

    @Override
    public List<ShopImage> getShopImages(Integer shopId) {
        // 将Integer类型的shopId转换为Long类型再传递
        return shopImageMapper.findImagesByShopId(shopId != null ? shopId.longValue() : null);
    }

    @Override
    public int countShops() {
        return shopMapper.countAllShops();
    }

}
