package com.fudan.xiaozhong_dianping.shop.controller;

import com.fudan.result.PageResult;
import com.fudan.result.Result;
import com.fudan.xiaozhong_dianping.shop.dto.ShopImportDTO;
import com.fudan.xiaozhong_dianping.shop.dto.ShopImportWithDescriptionsDTO;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    @ApiOperation(value="分页查询")
    public Result<List<Shop>> page(int pageCurrent,int pageSize){
        log.info("分页查询，当前页码：{}，每页记录数：{}",pageCurrent,pageSize);
        // 调用服务层获取分页数据
        List<Shop> list=shopService.showShops(pageCurrent,pageSize);
        return Result.success(list);
    }

    /**
     * 搜索店铺接口
     * 该方法用于处理GET请求，根据查询条件返回店铺列表
     *
     * @param shopPageQueryDTO 包含搜索条件和分页信息的DTO
     * @return 返回包含店铺列表的分页结果
     */
    @GetMapping("/search")
    public Result<List<Shop>> search(ShopPageQueryDTO shopPageQueryDTO) {
        log.info("搜索店铺，参数：{}", shopPageQueryDTO);
        
        // 设置默认分页参数
        if (shopPageQueryDTO.getPageSize() == null) {
            shopPageQueryDTO.setPageSize(10);
        }
        if (shopPageQueryDTO.getPageCurrent() == null) {
            shopPageQueryDTO.setPageCurrent(1);
        }
        
        // 计算偏移量
        shopPageQueryDTO.setOffset((shopPageQueryDTO.getPageCurrent() - 1) * shopPageQueryDTO.getPageSize());
        
        // 保存搜索历史
        if (shopPageQueryDTO.getUserId() != null && shopPageQueryDTO.getName() != null && !shopPageQueryDTO.getName().trim().isEmpty()) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(shopPageQueryDTO.getUserId());
            searchHistory.setSearchTime(new Date());
            searchHistory.setKeyword(shopPageQueryDTO.getName().trim());
            shopService.saveSearchHistory(searchHistory);
        }

        // 执行搜索
        List<Shop> pageResult = shopService.searchShops(shopPageQueryDTO);
        log.info("搜索完成，找到{}条记录", pageResult.size());
        
        return Result.success(pageResult);
    }

    /**
     * 获取用户的搜索历史记录
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
     * @param shopId 商家ID
     * @return 商家详情及图片
     */
    @GetMapping("/{shopId}/detail")
    public Map<String, Object> getShopDetails(@PathVariable("shopId") Long shopId) {
        return shopService.getShopDetails(shopId);
    }

    /**
     * 删除用户的搜索历史记录
     * @param userId 用户ID
     * @param historyId 历史记录ID
     * @return 操作结果
     */
    @DeleteMapping("/search/history/{historyId}")
    public Result<Void> deleteSearchHistory(@RequestParam Long userId, @PathVariable Long historyId) {
        log.info("删除用户的搜索历史记录：userId={}, historyId={}", userId, historyId);
        shopService.deleteSearchHistory(userId, historyId);
        return Result.success();
    }

    /**
     * 清空用户的搜索历史记录
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/search/history/clear")
    public Result<Void> clearSearchHistory(@RequestParam Long userId) {
        log.info("清空用户的搜索历史记录：userId={}", userId);
        shopService.clearSearchHistory(userId);
        return Result.success();
    }
}