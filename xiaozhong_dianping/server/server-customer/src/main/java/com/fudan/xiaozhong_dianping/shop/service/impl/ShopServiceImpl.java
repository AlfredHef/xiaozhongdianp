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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * ShopServiceImpl类实现了ShopService接口，提供了一系列与商店相关的服务方法
 * 它使用了Spring的@Service注解，标志着它是一个服务层组件
 */
public class ShopServiceImpl implements ShopService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ShopServiceImpl.class);

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
    
    /**
     * 在Spring容器刷新时检查搜索历史表是否存在
     * 
     * @param event Spring上下文刷新事件
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            // 检查表是否存在
            Integer exists = searchHistoryMapper.checkTableExists();
            if (exists != null && exists == 1) {
                log.info("搜索历史表(search_history)存在，可以正常使用");
                
                // 进一步检查表的字段结构
                try {
                    log.info("开始检查搜索历史表结构...");
                    
                    // 模拟搜索历史保存，测试表结构
                    SearchHistory testHistory = SearchHistory.builder()
                        .userId(0L) // 测试用户ID
                        .keyword("test_table_structure")
                        .searchTime(new Date())
                        .build();
                    
                    // 尝试插入记录
                    int result = searchHistoryMapper.insert(testHistory);
                    
                    if (result > 0) {
                        log.info("搜索历史表结构验证成功，测试记录ID: {}", testHistory.getId());
                        // 删除测试数据
                        searchHistoryMapper.deleteTestRecord(testHistory.getId());
                    } else {
                        log.warn("搜索历史表结构测试失败，无法插入测试记录");
                    }
                } catch (Exception e) {
                    log.error("搜索历史表结构测试失败: {}", e.getMessage(), e);
                }
            } else {
                log.error("搜索历史表(search_history)不存在！请检查数据库结构");
            }
        } catch (Exception e) {
            log.error("检查搜索历史表时发生错误: {}", e.getMessage(), e);
        }
    }

    @Override
    /**
     * 保存用户的搜索历史记录
     *
     * @param searchHistory 搜索历史对象，包含用户ID和搜索关键词等信息
     * @return 如果插入操作成功，则返回true；否则返回false
     */
    public Boolean saveSearchHistory(SearchHistory searchHistory) {
        try {
            if (searchHistory == null || searchHistory.getUserId() == null || 
                searchHistory.getKeyword() == null || searchHistory.getKeyword().trim().isEmpty()) {
                log.warn("保存搜索历史失败：传入的参数不完整 - {}", searchHistory);
                return false;
            }
            
            log.info("尝试保存搜索历史: userId={}, keyword={}",
                    searchHistory.getUserId(), searchHistory.getKeyword());
            
            // 确保搜索关键词不超过数据库字段长度限制
            String keyword = searchHistory.getKeyword().trim();
            if (keyword.length() > 100) { // 假设数据库字段长度为100
                keyword = keyword.substring(0, 100);
                searchHistory.setKeyword(keyword);
                log.warn("搜索关键词过长，已自动截断: {}", keyword);
            }
            
            // 确保搜索时间存在
            if (searchHistory.getSearchTime() == null) {
                searchHistory.setSearchTime(new Date());
            }
            
            int result = searchHistoryMapper.insert(searchHistory);
            
            if (result > 0) {
                log.info("搜索历史保存成功: userId={}, keyword={}", 
                        searchHistory.getUserId(), searchHistory.getKeyword());
                return true;
            } else {
                log.warn("搜索历史保存失败: 数据库未插入记录");
                return false;
            }
        } catch (Exception e) {
            log.error("保存搜索历史时发生异常: " + e.getMessage(), e);
            return false;
        }
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
        
        // 打印每个商家的分类信息用于调试
        for (Shop shop : result) {
            log.info("搜索结果 - 商家ID: {}, 名称: {}, 分类ID: {}, 分类名称: {}", 
                shop.getId(), shop.getName(), shop.getCategoryId(), shop.getCategoryName());
        }
        
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
        if (userId == null) {
            log.warn("获取搜索历史记录失败: 用户ID为空");
            return Collections.emptyList();
        }
        
        try {
            log.info("查询用户的搜索历史记录: userId={}", userId);
            List<SearchHistory> historyList = searchHistoryMapper.getSearchHistoryByUserId(userId);
            log.info("获取到{}条搜索历史记录", historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("获取搜索历史记录时发生异常: " + e.getMessage(), e);
            return Collections.emptyList();
        }
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
    public Boolean clearSearchHistory(Long userId) {
        if (userId == null) {
            log.warn("清空搜索历史记录失败: 用户ID为空");
            return false;
        }
        
        try {
            log.info("尝试清空用户搜索历史: userId={}", userId);
            int result = searchHistoryMapper.deleteByUserId(userId);
            log.info("删除了{}条搜索历史记录", result);
            return result >= 0; // 即使没有记录被删除，操作仍然是成功的
        } catch (Exception e) {
            log.error("清空搜索历史记录时发生异常: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<ShopImage> getShopImages(Integer shopId) {
        log.info("开始获取商家图片，商家ID: {}", shopId);
        
        // 将Integer类型的shopId转换为Long类型再传递
        Long shopIdLong = shopId != null ? shopId.longValue() : null;
        
        if (shopIdLong == null) {
            log.warn("商家ID为空，无法获取图片");
            return new ArrayList<>();
        }
        
        List<ShopImage> images = shopImageMapper.findImagesByShopId(shopIdLong);
        log.info("商家[{}]获取到{}张图片", shopId, images.size());
        
        // 处理图片URL，只在非完整URL时添加前缀
        images.forEach(image -> {
            String imageUrl = image.getImageUrl();
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                // 移除开头的斜杠（如果有）
                while (imageUrl.startsWith("/")) {
                    imageUrl = imageUrl.substring(1);
                }
                // 添加前缀
                imageUrl = "/static/" + imageUrl;
                image.setImageUrl(imageUrl);
                log.debug("处理后的图片URL: {}", imageUrl);
            }
        });
        
        return images;
    }

    @Override
    public int countShops() {
        return shopMapper.countAllShops();
    }

}
