package com.fudan.xiaozhong_dianping.shop.service.impl;

import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.entity.ShopImage;
import com.fudan.xiaozhong_dianping.shop.mapper.SearchHistoryMapper;
import com.fudan.xiaozhong_dianping.shop.mapper.ShopMapper;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * ShopServiceImpl类实现了ShopService接口，提供了一系列与商店相关的服务方法
 * 它使用了Spring的@Service注解，标志着它是一个服务层组件
 */
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Override
    public Boolean saveSearchHistory(SearchHistory searchHistory) {
        int result = searchHistoryMapper.insert(searchHistory);
        return result > 0;
    }

    @Override
    public List<Shop> searchShops(ShopPageQueryDTO shopPageQueryDTO) {
        // 假设 shopPageQueryDTO 包含了分页参数 pageCurrent 和 pageSize
        int offset = (shopPageQueryDTO.getPageCurrent() - 1) * shopPageQueryDTO.getPageSize();
        int limit = shopPageQueryDTO.getPageSize();
        return shopMapper.showShops(offset, limit);
    }

    @Override
    public List<SearchHistory> getSearchHistoryByUserId(Long userId) {
        return searchHistoryMapper.getSearchHistoryByUserId(userId);
    }

    @Override
    public List<Shop> showShops(int pageCurrent, int pageSize) {
        int offset = (pageCurrent - 1) * pageSize;
        return shopMapper.showShops(offset, pageSize);
    }

    @Override
    @Transactional
    public void importShopsFromCSV(List<Shop> shops, List<String> imagePaths) {
        if (shops == null || shops.isEmpty()) {
            throw new IllegalArgumentException("商家列表不能为空");
        }

        if (imagePaths == null || imagePaths.isEmpty()) {
            throw new IllegalArgumentException("图片路径列表不能为空");
        }

        // 检查商家数量与图片数量是否匹配
        if (shops.size() != imagePaths.size()) {
            throw new IllegalArgumentException("商家数量与图片数量不匹配");
        }

        // 批量插入商家数据
        shopMapper.batchInsertShops(shops);

        // 准备批量插入的图片数据
        List<ShopImage> shopImages = new ArrayList<>();
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            String imagePath = imagePaths.get(i);

            Long shopId = Long.valueOf(shop.getId());
            if (shopId == null) {
                throw new IllegalStateException("商家ID未正确生成");
            }

            // 假设描述信息可以从图片路径中提取或设置为默认值
            String description = "描述信息"; // 根据实际需求设置

            shopImages.add(new ShopImage(shopId, null, imagePath, description)); // 根据实际需求设置 description
        }

        // 批量插入图片数据
        if (!shopImages.isEmpty()) {
            shopMapper.batchInsertShopImages(shopImages);
        }
    }

    @Override
    @Transactional
    public void importShopsFromCSVWithDescriptions(List<Shop> shops, List<ShopImage> shopImages) {
        if (shops == null || shops.isEmpty()) {
            throw new IllegalArgumentException("商家列表不能为空");
        }

        if (shopImages == null || shopImages.isEmpty()) {
            throw new IllegalArgumentException("图片信息列表不能为空");
        }

        // 批量插入商家数据
        shopMapper.batchInsertShops(shops);

        // 确保每个 ShopImage 都有正确的 shopId
        for (ShopImage shopImage : shopImages) {
            if (shopImage.getShopId() == null) {
                throw new IllegalStateException("ShopImage 中的 shopId 不能为空");
            }
        }

        // 如果需要，可以在这里补充其他逻辑，例如验证图片路径等

        // 批量插入图片数据
        shopMapper.batchInsertShopImages(shopImages);
    }
}
