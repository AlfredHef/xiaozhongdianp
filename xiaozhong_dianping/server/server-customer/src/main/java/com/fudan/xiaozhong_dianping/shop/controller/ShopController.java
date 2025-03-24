package com.fudan.xiaozhong_dianping.shop.controller;

import com.fudan.result.PageResult;
import com.fudan.result.Result;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

// 控制器类，处理与店铺相关的请求
@RequestMapping("/shop")
@RestController
public class ShopController {

    // 自动注入ShopService，用于处理店铺相关的业务逻辑
    @Autowired
    private ShopService shopService;

    /**
     * 搜索店铺接口
     * 该方法用于处理GET请求，根据查询条件返回店铺列表
     *
     * @param shopPageQueryDTO 包含搜索条件和分页信息的DTO
     * @return 返回包含店铺列表的分页结果
     */
    @GetMapping("/search")
    public Result<List<Shop>> search(ShopPageQueryDTO shopPageQueryDTO) {
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
        List<SearchHistory> historyList = shopService.getSearchHistoryByUserId(userId);
        return Result.success(historyList);
    }
}