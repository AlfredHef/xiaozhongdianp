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
        log.info("搜索店铺：{}", shopPageQueryDTO);
        // 创建搜索历史记录对象
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setUserId(shopPageQueryDTO.getUserId());
        // 设置搜索时间
        searchHistory.setSearchTime(new Date());
        // 设置搜索关键词
        searchHistory.setKeyword(shopPageQueryDTO.getName());
        // 保存搜索历史记录
        shopService.saveSearchHistory(searchHistory);

        // 调用服务层方法，根据查询条件搜索店铺，并返回分页结果
        List<Shop> pageResult = shopService.searchShops(shopPageQueryDTO);
        // 返回成功结果，包含分页数据
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
     * 从CSV导入商家数据（带图片路径）
     * @param shopImportDTO 包含商家列表和图片路径列表的DTO
     * @return 导入结果
     */
    @PostMapping("/import-with-images")
    public Result<String> importShopsWithImages(@RequestBody ShopImportDTO shopImportDTO) {
        try {
            shopService.importShopsFromCSV(shopImportDTO.getShops(), shopImportDTO.getImagePaths());
            return Result.success("商家数据导入成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("商家数据导入失败: " + e.getMessage());
        }
    }

    /**
     * 从CSV导入商家数据（带完整图片信息）
     * @param shopImportWithDescriptionsDTO 包含商家列表和图片信息列表的DTO
     * @return 导入结果
     */
    @PostMapping("/import-with-descriptions")
    public Result<String> importShopsWithDescriptions(
            @RequestBody ShopImportWithDescriptionsDTO shopImportWithDescriptionsDTO) {
        try {
            shopService.importShopsFromCSVWithDescriptions(
                    shopImportWithDescriptionsDTO.getShops(),
                    shopImportWithDescriptionsDTO.getShopImages());
            return Result.success("商家数据导入成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("商家数据导入失败: " + e.getMessage());
        }
    }
}