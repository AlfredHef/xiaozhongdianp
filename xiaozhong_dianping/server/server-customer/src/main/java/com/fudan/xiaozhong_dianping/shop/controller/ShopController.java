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
     * @param request 请求对象
     * @return 返回包含店铺列表的分页结果
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(ShopPageQueryDTO shopPageQueryDTO, HttpServletRequest request) {
        // 记录完整的请求URL和参数
        log.info("搜索店铺，完整请求URL: {}", request.getRequestURL() + "?" + request.getQueryString());
        log.info("搜索店铺，绑定后的DTO参数：{}", shopPageQueryDTO);
        
        // 验证userId参数
        if (shopPageQueryDTO.getUserId() != null) {
            log.info("接收到用户ID: {}, 类型: {}", shopPageQueryDTO.getUserId(), shopPageQueryDTO.getUserId().getClass().getName());
        } else {
            // 尝试从原始请求参数获取userId
            String userIdParam = request.getParameter("userId");
            log.info("从请求参数获取原始userId: {}", userIdParam);
            
            if (userIdParam != null && !userIdParam.isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdParam);
                    log.info("手动解析userId参数: {}", userId);
                    shopPageQueryDTO.setUserId(userId);
                } catch (NumberFormatException e) {
                    log.warn("userId参数无法转为Long类型: {}", userIdParam);
                }
            } else {
                log.warn("请求中不包含userId参数，搜索历史将不会被记录");
            }
        }
        
        try {
            setDefaultPageParams(shopPageQueryDTO);
            validateNumericParams(shopPageQueryDTO);
            saveSearchHistoryIfNeeded(shopPageQueryDTO);

            // 执行搜索
            List<Shop> shopList = shopService.searchShops(shopPageQueryDTO);
            log.info("搜索完成，找到{}条记录", shopList.size());

            // 检查商家分类ID是否存在
            for (Shop shop : shopList) {
                log.debug("商家[{}]的分类ID：{}，分类名称：{}",
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
     * 清空指定用户的所有搜索历史
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/search/history/clear")
    public Result<Boolean> clearSearchHistory(@RequestParam Long userId) {
        log.info("清空用户的搜索历史记录：{}", userId);
        try {
            Boolean result = shopService.clearSearchHistory(userId);
            if (result) {
                log.info("用户{}的搜索历史已成功清空", userId);
                return Result.success(true);
            } else {
                log.warn("清空用户{}的搜索历史失败", userId);
                return Result.error("清空搜索历史失败");
            }
        } catch (Exception e) {
            log.error("清空搜索历史时发生错误", e);
            return Result.error("系统错误：" + e.getMessage());
        }
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
        try {
            if (shopPageQueryDTO.getUserId() != null && 
                shopPageQueryDTO.getName() != null && 
                !shopPageQueryDTO.getName().trim().isEmpty()) {
                
                log.info("保存搜索历史 - 用户ID: {}, 关键词: {}", 
                    shopPageQueryDTO.getUserId(), 
                    shopPageQueryDTO.getName().trim());
                
                SearchHistory searchHistory = SearchHistory.builder()
                    .userId(shopPageQueryDTO.getUserId())
                    .keyword(shopPageQueryDTO.getName().trim())
                    .searchTime(new Date())
                    .build();
                
                try {
                    Boolean saved = shopService.saveSearchHistory(searchHistory);
                    if (saved) {
                        log.info("搜索历史保存成功");
                    } else {
                        log.warn("搜索历史保存失败");
                    }
                } catch (Exception e) {
                    log.error("保存搜索历史发生具体错误: {}", e.getMessage(), e);
                    // 检查是否为外键约束错误
                    if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                        log.error("外键约束错误 - 可能是用户ID不存在: {}", shopPageQueryDTO.getUserId());
                    }
                }
            } else {
                // 记录更详细的缺失信息
                if (shopPageQueryDTO.getUserId() == null) {
                    log.warn("不满足保存搜索历史条件 - 缺少userId");
                } else if (shopPageQueryDTO.getName() == null) {
                    log.warn("不满足保存搜索历史条件 - 缺少搜索关键词");
                } else {
                    log.warn("不满足保存搜索历史条件 - 搜索关键词为空");
                }
                
                log.debug("不满足保存搜索历史的条件 - userId: {}, keyword: {}", 
                    shopPageQueryDTO.getUserId(), 
                    shopPageQueryDTO.getName());
            }
        } catch (Exception e) {
            log.error("保存搜索历史时发生错误", e);
            // 不抛出异常，避免影响正常的搜索流程
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

    /**
     * 测试搜索历史保存功能
     * 用于手动添加搜索历史记录
     */
    @GetMapping("/search/history/test")
    public Result<?> testAddSearchHistory(@RequestParam Long userId, @RequestParam String keyword) {
        log.info("测试添加搜索历史 - 用户ID: {}, 关键词: {}", userId, keyword);
        
        try {
            SearchHistory searchHistory = SearchHistory.builder()
                .userId(userId)
                .keyword(keyword)
                .searchTime(new Date())
                .build();
            
            Boolean result = shopService.saveSearchHistory(searchHistory);
            
            if (result) {
                log.info("测试 - 搜索历史保存成功");
                return Result.success("搜索历史添加成功");
            } else {
                log.warn("测试 - 搜索历史保存失败");
                return Result.error("搜索历史添加失败");
            }
        } catch (Exception e) {
            log.error("测试 - 保存搜索历史时发生错误: {}", e.getMessage(), e);
            
            // 检查是否为外键约束错误
            if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                return Result.error("外键约束错误 - 可能是用户ID不存在: " + userId);
            }
            
            return Result.error("添加搜索历史失败: " + e.getMessage());
        }
    }
}
