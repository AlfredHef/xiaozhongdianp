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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主店铺控制器
 * 注意：此控制器主要作为兼容旧API的分发器，大部分功能已迁移到更具体的控制器中
 * @see ShopBaseController - 处理基本的店铺操作
 * @see ShopSearchController - 处理搜索相关的功能
 * @see SearchHistoryController - 处理搜索历史相关的功能
 */
@RequestMapping("/shop")
@RestController
@Slf4j
public class ShopController {

    @Autowired
    private ShopBaseController shopBaseController;
    
    @Autowired
    private ShopSearchController shopSearchController;
    
    @Autowired
    private SearchHistoryController searchHistoryController;

    /**
     * 分页查询店铺信息 - 兼容旧API
     * @param pageCurrent 当前页码
     * @param pageSize 每页记录数
     * @return 店铺列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询")
    public Result<List<Map<String, Object>>> page(int pageCurrent, int pageSize) {
        log.info("转发分页查询请求到ShopBaseController");
        return shopBaseController.page(pageCurrent, pageSize);
    }

    /**
     * 搜索店铺接口 - 兼容旧API
     * @param shopPageQueryDTO 查询条件
     * @return 店铺列表
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(ShopPageQueryDTO shopPageQueryDTO, HttpServletRequest request) {
        log.info("转发搜索请求到ShopSearchController");
        return shopSearchController.search(shopPageQueryDTO, request);
    }

    /**
     * 获取用户的搜索历史记录 - 兼容旧API
     * @param userId 用户ID
     * @return 搜索历史记录列表
     */
    @GetMapping("/search/history")
    public Result<List<SearchHistory>> getSearchHistory(@RequestParam Long userId) {
        log.info("转发获取搜索历史请求到SearchHistoryController");
        return searchHistoryController.getSearchHistory(userId);
    }

    /**
     * 获取商家详情 - 兼容旧API
     * @param shopId 商家ID
     * @return 商家详情及图片
     */
    @GetMapping("/{shopId}/detail")
    public Map<String, Object> getShopDetails(@PathVariable("shopId") Long shopId) {
        log.info("转发获取商家详情请求到ShopBaseController");
        return shopBaseController.getShopDetails(shopId);
    }

    /**
     * 获取首页商家列表 - 兼容旧API
     * @param pageCurrent 当前页码
     * @param pageSize 每页记录数
     * @return 商家列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getShopList(
            @RequestParam(defaultValue = "1") Integer pageCurrent,
            @RequestParam(defaultValue = "4") Integer pageSize) {
        log.info("转发获取首页商家列表请求到ShopBaseController");
        return shopBaseController.getShopList(pageCurrent, pageSize);
    }

    /**
     * 清空用户的搜索历史记录 - 兼容旧API
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/search/history/clear")
    public Result<Boolean> clearSearchHistory(@RequestParam Long userId) {
        log.info("转发清空搜索历史请求到SearchHistoryController");
        return searchHistoryController.clearSearchHistory(userId);
    }
}
