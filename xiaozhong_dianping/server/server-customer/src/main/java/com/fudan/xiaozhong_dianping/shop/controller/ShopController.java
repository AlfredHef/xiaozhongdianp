package com.fudan.xiaozhong_dianping.shop.controller;

import com.fudan.result.Result;
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

    /**
     * 分页查询店铺信息
     *
     * @param pageCurrent 当前页码（从1开始计数）
     * @param pageSize 每页记录数
     * @return 包含分页查询结果和状态信息的响应对象，data属性为店铺列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询")
    public Result<List<Map<String, Object>>> page(int pageCurrent, int pageSize) {
        log.info("分页查询，当前页码：{}，每页记录数：{}", pageCurrent, pageSize);

        // 计算偏移量
        int offset = (pageCurrent - 1) * pageSize;

        // 调用服务层获取分页数据
        List<Shop> shops = shopService.showShops(offset, pageSize);

        // 为商家添加图片信息
        List<Map<String, Object>> result = getShopDataWithImages(shops);

        return Result.success(result);
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
            setDefaultPageParams(shopPageQueryDTO);
            validateNumericParams(shopPageQueryDTO);
            saveSearchHistoryIfNeeded(shopPageQueryDTO);

            // 执行搜索
            List<Shop> shopList = shopService.searchShops(shopPageQueryDTO);
            log.info("搜索完成，找到{}条记录", shopList.size());

            // 检查商家分类ID是否存在
            for (Shop shop : shopList) {
                log.info("商家[{}]的分类ID：{}，分类名称：{}",
                    shop.getId(), shop.getCategoryId(), shop.getCategoryName());
            }

            // 为每个商家获取图片信息
            List<Map<String, Object>> result = getShopDataWithImages(shopList);

            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索店铺时发生错误：", e);
            return Result.error("搜索失败：" + e.getMessage());
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
     * 删除用户的搜索历史记录
     *
     * @param historyId 历史记录ID
     * @return 操作结果
     */
    @DeleteMapping("/search/history/{historyId}")
    public Result<Void> deleteSearchHistory(@PathVariable Long historyId) {
        log.info("删除用户的搜索历史记录： historyId={}", historyId);
        shopService.deleteSearchHistory(historyId);
        return Result.success();
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
        if (shopPageQueryDTO.getMinRating() != null && (shopPageQueryDTO.getMinRating() < 0 || shopPageQueryDTO.getMinRating() > 5)) {
            shopPageQueryDTO.setMinRating(null);
        }
        if (shopPageQueryDTO.getMaxRating() != null && (shopPageQueryDTO.getMaxRating() < 0 || shopPageQueryDTO.getMaxRating() > 5)) {
            shopPageQueryDTO.setMaxRating(null);
        }
        if (shopPageQueryDTO.getMinPrice() != null && shopPageQueryDTO.getMinPrice() < 0) {
            shopPageQueryDTO.setMinPrice(null);
        }
        if (shopPageQueryDTO.getMaxPrice() != null && shopPageQueryDTO.getMaxPrice() < 0) {
            shopPageQueryDTO.setMaxPrice(null);
        }
        if (shopPageQueryDTO.getMinAverageCost() != null && shopPageQueryDTO.getMinAverageCost() < 0) {
            shopPageQueryDTO.setMinAverageCost(null);
        }
        if (shopPageQueryDTO.getMaxAverageCost() != null && shopPageQueryDTO.getMaxAverageCost() < 0) {
            shopPageQueryDTO.setMaxAverageCost(null);
        }
    }

    /**
     * 保存搜索历史
     * @param shopPageQueryDTO 查询参数
     */
    private void saveSearchHistoryIfNeeded(ShopPageQueryDTO shopPageQueryDTO) {
        if (shopPageQueryDTO.getUserId() != null && shopPageQueryDTO.getName() != null && !shopPageQueryDTO.getName().trim().isEmpty()) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(shopPageQueryDTO.getUserId());
            searchHistory.setSearchTime(new Date());
            searchHistory.setKeyword(shopPageQueryDTO.getName().trim());
            shopService.saveSearchHistory(searchHistory);
        }
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
}
