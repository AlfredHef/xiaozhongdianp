package com.fudan.xiaozhong_dianping.shop.controller;

import com.fudan.result.Result;
import com.fudan.xiaozhong_dianping.common.utils.SimilarCharsUtil;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 控制器类，处理与店铺相关的请求
@RequestMapping("/shop")
@RestController
@Slf4j
public class ShopController {

    // 自动注入ShopService，用于处理店铺相关的业务逻辑
    @Autowired
    private ShopService shopService;

    @Autowired
    private SimilarCharsUtil similarCharsUtil;

    /**
     * 分页查询店铺信息
     *
     * @param pageCurrent 当前页码（从1开始计数）
     * @param pageSize 每页记录数
     * @return 包含分页查询结果和状态信息的响应对象，data属性为店铺列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询")
    public Result<List<Map<String, Object>>> page(
            @RequestParam(required = false) Integer pageCurrent,
            @RequestParam(required = false) Integer pageSize) {
        try {
            // 检查参数是否为空
            if (pageCurrent == null || pageSize == null) {
                String errorMessage = "分页查询参数缺失，pageCurrent和pageSize不能为空";
                log.error(errorMessage);
                return Result.error(errorMessage);
            }

            // 计算偏移量
            int offset = (pageCurrent - 1) * pageSize;

            // 调用服务层获取分页数据
            List<Shop> shops = shopService.showShops(offset, pageSize);

            // 为商家添加图片信息
            List<Map<String, Object>> result = getShopDataWithImages(shops);

            return Result.success(result);
        } catch (Exception e) {
            String errorMessage = "分页查询时发生错误：" + e.getMessage();
            log.error(errorMessage, e);
            return Result.error(errorMessage);
        }
    }

    /**
     * 搜索店铺接口
     * 该方法用于处理GET请求，根据查询条件返回店铺列表
     *
     * @param shopPageQueryDTO 包含搜索条件和分页信息的DTO
     * @return 返回包含店铺列表的分页结果
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(ShopPageQueryDTO shopPageQueryDTO) {
        log.info("搜索店铺，参数：{}", shopPageQueryDTO);
        try {
            // 预处理查询参数
            prepareSearchQuery(shopPageQueryDTO);
            
            // 执行搜索并获取结果
            List<Shop> shopList = executeSearch(shopPageQueryDTO);
            
            // 转换搜索结果为包含图片的格式
            List<Map<String, Object>> result = getShopDataWithImages(shopList);

            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索店铺时发生错误：", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 预处理搜索查询参数
     * @param queryDTO 查询参数
     */
    private void prepareSearchQuery(ShopPageQueryDTO queryDTO) {
        // 设置默认分页参数
        setDefaultPageParams(queryDTO);
        
        // 验证并修正数值型参数
        validateNumericParams(queryDTO);
        
        // 扩展搜索关键词，添加形近字匹配
        expandSearchKeywords(queryDTO);
        
        // 保存搜索历史（如果需要）
        saveSearchHistoryIfNeeded(queryDTO);
    }
    
    /**
     * 执行搜索并处理结果
     * @param queryDTO 查询参数
     * @return 搜索结果列表
     */
    private List<Shop> executeSearch(ShopPageQueryDTO queryDTO) {
        // 执行搜索
        List<Shop> shopList = shopService.searchShops(queryDTO);
        log.info("搜索完成，找到{}条记录", shopList.size());

        // 记录搜索结果的详细信息（用于调试）
        logSearchResults(shopList);
        
        return shopList;
    }
    
    /**
     * 记录搜索结果的详细信息
     * @param shopList 商店列表
     */
    private void logSearchResults(List<Shop> shopList) {
        if (shopList.isEmpty()) {
            log.info("搜索结果为空");
            return;
        }
        
        // 仅记录详细信息（如分类ID）用于调试
        for (Shop shop : shopList) {
            log.info("搜索结果 - 商家ID: {}, 名称: {}, 分类ID: {}, 分类名称: {}", 
                shop.getId(), shop.getName(), shop.getCategoryId(), shop.getCategoryName());
        }
    }

    /**
     * 获取用户的搜索历史记录
     *
     * @param userId 用户ID
     * @return 搜索历史记录列表
     */
    @GetMapping("/search/history")
    public Result<List<SearchHistory>> getSearchHistory(@RequestParam Long userId) {
        log.info("获取用户的搜索历史记录：{}", userId);
        List<SearchHistory> historyList = shopService.getSearchHistoryByUserId(userId);
        return Result.success(historyList);
    }

    /**
     * 获取商家详情
     *
     * @param shopId 商家ID
     * @return 商家详情及图片
     */
    @GetMapping("/{shopId}/detail")
    public Map<String, Object> getShopDetails(@PathVariable("shopId") Long shopId) {
        return shopService.getShopDetails(shopId);
    }

    /**
     * 获取首页商家列表
     *
     * @param pageCurrent 当前页码，默认1
     * @param pageSize 每页记录数，默认4
     * @return 商家列表及分页信息
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getShopList(
            @RequestParam(defaultValue = "1") Integer pageCurrent,
            @RequestParam(defaultValue = "4") Integer pageSize) {
        log.info("获取首页商家列表，页码：{}，每页记录数：{}", pageCurrent, pageSize);

        // 计算偏移量
        int offset = (pageCurrent - 1) * pageSize;

        // 获取商家列表
        List<Shop> shops = shopService.showShops(offset, pageSize);

        // 获取商家总数
        int total = shopService.countShops();

        // 为每个商家获取图片信息
        List<Map<String, Object>> shopsList = getShopDataWithImages(shops);

        // 构建返回结果，包含分页信息
        Map<String, Object> result = buildPaginationResult(shopsList, total, pageCurrent, pageSize);

        return Result.success(result);
    }

    /**
     * 清空用户的搜索历史记录
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/search/history/clear")
    public Result<Boolean> clearSearchHistory(@RequestParam Long userId) {
        log.info("清空用户的搜索历史记录：userId={}", userId);
        Boolean result = shopService.clearSearchHistory(userId);
        return Result.success(result);
    }

    /**
     * 设置默认分页参数
     * @param shopPageQueryDTO 查询参数
     */
    private void setDefaultPageParams(ShopPageQueryDTO shopPageQueryDTO) {
        if (shopPageQueryDTO.getPageSize() == null) {
            shopPageQueryDTO.setPageSize(10);
        }
        if (shopPageQueryDTO.getPageCurrent() == null) {
            shopPageQueryDTO.setPageCurrent(1);
        }
        shopPageQueryDTO.setOffset((shopPageQueryDTO.getPageCurrent() - 1) * shopPageQueryDTO.getPageSize());
    }

    /**
     * 验证数值型参数
     * @param shopPageQueryDTO 查询参数
     */
    private void validateNumericParams(ShopPageQueryDTO shopPageQueryDTO) {
        // 验证评分范围
        validateRatingRange(shopPageQueryDTO);
        
        // 验证价格范围
        validatePriceRange(shopPageQueryDTO);
        
        // 验证人均消费范围
        validateAverageCostRange(shopPageQueryDTO);
    }
    
    /**
     * 验证评分范围参数
     * @param queryDTO 查询参数
     */
    private void validateRatingRange(ShopPageQueryDTO queryDTO) {
        // 验证最小评分，范围应为0-5
        if (isOutOfRange(queryDTO.getMinRating(), 0, 5)) {
            queryDTO.setMinRating(null);
        }
        
        // 验证最大评分，范围应为0-5
        if (isOutOfRange(queryDTO.getMaxRating(), 0, 5)) {
            queryDTO.setMaxRating(null);
        }
    }
    
    /**
     * 验证价格范围参数
     * @param queryDTO 查询参数
     */
    private void validatePriceRange(ShopPageQueryDTO queryDTO) {
        // 验证最小价格，不应小于0
        if (isLessThan(queryDTO.getMinPrice(), 0)) {
            queryDTO.setMinPrice(null);
        }
        
        // 验证最大价格，不应小于0
        if (isLessThan(queryDTO.getMaxPrice(), 0)) {
            queryDTO.setMaxPrice(null);
        }
    }
    
    /**
     * 验证人均消费范围参数
     * @param queryDTO 查询参数
     */
    private void validateAverageCostRange(ShopPageQueryDTO queryDTO) {
        // 验证最小人均消费，不应小于0
        if (isLessThan(queryDTO.getMinAverageCost(), 0)) {
            queryDTO.setMinAverageCost(null);
        }
        
        // 验证最大人均消费，不应小于0
        if (isLessThan(queryDTO.getMaxAverageCost(), 0)) {
            queryDTO.setMaxAverageCost(null);
        }
    }
    
    /**
     * 判断值是否在指定范围外
     * @param value 要检查的值
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 如果值为null或超出范围则返回true
     */
    private boolean isOutOfRange(Double value, double min, double max) {
        return value != null && (value < min || value > max);
    }
    
    /**
     * 判断值是否小于指定的最小值
     * @param value 要检查的值
     * @param min 最小值
     * @return 如果值为null或小于最小值则返回true
     */
    private boolean isLessThan(Integer value, int min) {
        return value != null && value < min;
    }
    
    /**
     * 判断Double类型值是否小于指定的最小值
     * @param value 要检查的值
     * @param min 最小值
     * @return 如果值为null或小于最小值则返回true
     */
    private boolean isLessThan(Double value, double min) {
        return value != null && value < min;
    }

    /**
     * 保存搜索历史
     * @param shopPageQueryDTO 查询参数
     */
    private void saveSearchHistoryIfNeeded(ShopPageQueryDTO shopPageQueryDTO) {
        // 提前判断是否有必要保存搜索历史
        if (!isValidForSearchHistory(shopPageQueryDTO)) {
            return;
        }
        
        // 创建并保存搜索历史记录
        SearchHistory searchHistory = createSearchHistoryFromQuery(shopPageQueryDTO);
        shopService.saveSearchHistory(searchHistory);
    }
    
    /**
     * 判断查询条件是否适合保存为搜索历史
     * @param queryDTO 查询参数
     * @return 如果满足条件返回true，否则返回false
     */
    private boolean isValidForSearchHistory(ShopPageQueryDTO queryDTO) {
        // 用户ID必须存在
        if (queryDTO.getUserId() == null) {
            return false;
        }
        
        // 搜索关键词必须存在且不为空
        String keyword = queryDTO.getName();
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 从查询条件创建搜索历史对象
     * @param queryDTO 查询参数
     * @return 创建的搜索历史对象
     */
    private SearchHistory createSearchHistoryFromQuery(ShopPageQueryDTO queryDTO) {
        SearchHistory history = new SearchHistory();
        history.setUserId(queryDTO.getUserId());
        history.setSearchTime(new Date());
        history.setKeyword(queryDTO.getName().trim());
        return history;
    }

    /**
     * 获取包含店铺信息和图片的列表
     * @param shopList 店铺列表
     * @return 包含店铺信息和图片的列表
     */
    private List<Map<String, Object>> getShopDataWithImages(List<Shop> shopList) {
        return shopList.stream().map(shop -> {
            Map<String, Object> shopData = new HashMap<>();
            shopData.put("shop", shop);
            shopData.put("images", shopService.getShopImages(shop.getId()));
            return shopData;
        }).collect(Collectors.toList());
    }

    /**
     * 构建分页结果
     * @param shopsList 店铺列表
     * @param total 总数
     * @param pageCurrent 当前页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    private Map<String, Object> buildPaginationResult(List<Map<String, Object>> shopsList, int total, int pageCurrent, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", shopsList);
        result.put("total", total);
        result.put("pages", (int) Math.ceil((double) total / pageSize));
        result.put("current", pageCurrent);
        result.put("size", pageSize);
        return result;
    }

    /**
     * 扩展搜索关键词，支持形近字搜索
     * @param queryDTO 查询参数
     */
    private void expandSearchKeywords(ShopPageQueryDTO queryDTO) {
        String keyword = queryDTO.getName();
        if (keyword != null && !keyword.trim().isEmpty()) {
            try {
                log.info("开始扩展关键词: {}", keyword);
                
                // 保存原始关键词
                String originalKeyword = keyword.trim();
                
                // 获取扩展后的关键词列表（包含形近字变体）
                List<String> expandedKeywords = similarCharsUtil.getExpandedKeywords(originalKeyword);
                
                // 打印每个扩展关键词，用于调试
                StringBuilder keywordsStr = new StringBuilder();
                for (String kw : expandedKeywords) {
                    keywordsStr.append(kw).append(", ");
                }
                log.info("原始关键词: {}, 扩展关键词: {}", originalKeyword, keywordsStr.toString());
                
                // 明确设置扩展关键词
                queryDTO.setExpandedKeywords(expandedKeywords);
                
                // 验证扩展关键词是否成功设置
                if (queryDTO.getExpandedKeywords() == null) {
                    log.error("扩展关键词设置失败，仍为null");
                } else {
                    log.info("扩展关键词设置成功，数量: {}", queryDTO.getExpandedKeywords().size());
                }
            } catch (Exception e) {
                log.error("扩展关键词时发生错误: {}", e.getMessage(), e);
                // 出错时使用原始关键词
                queryDTO.setExpandedKeywords(List.of("%" + keyword.trim() + "%"));
                log.info("使用原始关键词作为备选: {}", keyword.trim());
            }
        } else {
            log.warn("关键词为空，无法扩展");
        }
    }

    /**
     * 获取与给定关键词相似的推荐关键词
     * 
     * @param keyword 用户输入的关键词
     * @return 相似关键词列表
     */
    @GetMapping("/search/similar")
    public Result<List<String>> getSimilarKeywords(@RequestParam String keyword) {
        log.info("获取相似关键词，原始关键词：{}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(List.of());
        }
        
        try {
            List<String> similarKeywords = similarCharsUtil.getSimilarKeywords(keyword.trim());
            return Result.success(similarKeywords);
        } catch (Exception e) {
            log.error("获取相似关键词时发生错误：", e);
            return Result.error("获取相似关键词失败：" + e.getMessage());
        }
    }
}
