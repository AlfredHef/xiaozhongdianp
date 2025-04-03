package com.fudan.xiaozhong_dianping.shop.controller;

import com.fudan.result.Result;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺基本操作控制器
 * 处理店铺的基本查询、详情等操作
 */
@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopBaseController {

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
     * 获取商家详情
     *
     * @param shopId 商家ID
     * @return 商家详情及图片
     */
    @GetMapping("/{shopId}/detail")
    public Map<String, Object> getShopDetails(@PathVariable("shopId") Long shopId) {
        log.info("获取商家详情，商家ID：{}", shopId);
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
     * 根据类别获取商家列表
     *
     * @param categoryId 类别ID
     * @param pageCurrent 当前页码，默认1
     * @param pageSize 每页记录数，默认10
     * @return 商家列表及分页信息
     */
    @GetMapping("/category/{categoryId}")
    public Result<Map<String, Object>> getShopsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageCurrent,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("根据类别获取商家列表，类别ID：{}，页码：{}，每页记录数：{}", categoryId, pageCurrent, pageSize);

        // 计算偏移量
        int offset = (pageCurrent - 1) * pageSize;

        // 获取商家列表
        // 注意：目前ShopService中没有实现这些方法，使用通用查询代替
        List<Shop> shops = shopService.showShops(offset, pageSize);
        
        // 过滤出特定类别的商店
        shops = shops.stream()
                .filter(shop -> categoryId.equals(shop.getCategoryId()))
                .collect(Collectors.toList());

        // 获取该类别下商家总数（简化处理，实际应从数据库获取）
        int total = shops.size();

        // 为每个商家获取图片信息
        List<Map<String, Object>> shopsList = getShopDataWithImages(shops);

        // 构建返回结果，包含分页信息
        Map<String, Object> result = buildPaginationResult(shopsList, total, pageCurrent, pageSize);

        return Result.success(result);
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