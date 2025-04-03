package com.fudan.xiaozhong_dianping.shop.controller;

import com.fudan.result.Result;
import com.fudan.xiaozhong_dianping.shop.dto.ShopPageQueryDTO;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处理商家搜索相关请求的控制器
 */
@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopSearchController {

    @Autowired
    private ShopService shopService;

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
    
    /**
     * 临时修复搜索历史功能的接口
     */
    @GetMapping("/fixSearch")
    public Result<List<Map<String, Object>>> fixSearchHistory(ShopPageQueryDTO shopPageQueryDTO, HttpServletRequest request) {
        log.info("修复搜索历史接口 - 参数: {}", shopPageQueryDTO);
        
        try {
            setDefaultPageParams(shopPageQueryDTO);
            validateNumericParams(shopPageQueryDTO);
            
            // 尝试手动保存搜索历史
            if (shopPageQueryDTO.getUserId() != null && shopPageQueryDTO.getName() != null) {
                SearchHistory searchHistory = new SearchHistory();
                searchHistory.setUserId(shopPageQueryDTO.getUserId());
                searchHistory.setKeyword(shopPageQueryDTO.getName().trim());
                searchHistory.setSearchTime(new Date());
                
                try {
                    Boolean saved = shopService.saveSearchHistory(searchHistory);
                    if (saved) {
                        log.info("修复接口 - 搜索历史保存成功");
                    } else {
                        log.warn("修复接口 - 搜索历史保存失败");
                    }
                } catch (Exception e) {
                    log.error("修复接口 - 保存搜索历史时出错: {}", e.getMessage());
                }
            }
            
            // 继续正常搜索
            List<Shop> shopList = shopService.searchShops(shopPageQueryDTO);
            log.info("搜索完成，找到{}条记录", shopList.size());
            
            // 为每个商家获取图片信息
            List<Map<String, Object>> result = getShopDataWithImages(shopList);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("修复搜索接口发生错误: ", e);
            return Result.error("搜索失败: " + e.getMessage());
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
} 