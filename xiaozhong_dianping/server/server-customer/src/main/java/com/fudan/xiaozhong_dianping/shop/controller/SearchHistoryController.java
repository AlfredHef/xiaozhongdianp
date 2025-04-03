package com.fudan.xiaozhong_dianping.shop.controller;

import com.fudan.result.Result;
import com.fudan.xiaozhong_dianping.shop.entity.SearchHistory;
import com.fudan.xiaozhong_dianping.shop.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 搜索历史记录控制器
 * 专门处理与搜索历史相关的请求
 */
@RestController
@RequestMapping("/shop/search")
@Slf4j
public class SearchHistoryController {

    @Autowired
    private ShopService shopService;

    /**
     * 获取用户的搜索历史记录
     *
     * @param userId 用户ID
     * @return 搜索历史记录列表
     */
    @GetMapping("/history")
    public Result<List<SearchHistory>> getSearchHistory(@RequestParam Long userId) {
        log.info("获取用户的搜索历史记录：用户ID={}", userId);
        
        try {
            List<SearchHistory> historyList = shopService.getSearchHistoryByUserId(userId);
            log.info("成功获取到{}条搜索历史", historyList.size());
            return Result.success(historyList);
        } catch (Exception e) {
            log.error("获取搜索历史时发生错误: ", e);
            return Result.error("获取搜索历史失败: " + e.getMessage());
        }
    }

    /**
     * 清空用户的搜索历史记录
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/history/clear")
    public Result<Boolean> clearSearchHistory(@RequestParam Long userId) {
        log.info("清空用户的搜索历史记录：userId={}", userId);
        
        try {
            Boolean result = shopService.clearSearchHistory(userId);
            if (result) {
                log.info("成功清空用户[{}]的搜索历史", userId);
            } else {
                log.warn("清空用户[{}]的搜索历史失败", userId);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("清空搜索历史时发生错误: ", e);
            return Result.error("清空搜索历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动添加搜索历史记录
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 操作结果
     */
    @PostMapping("/history/add")
    public Result<Boolean> addSearchHistory(@RequestParam Long userId, @RequestParam String keyword) {
        log.info("手动添加搜索历史记录 - 用户ID: {}, 关键词: {}", userId, keyword);
        
        try {
            SearchHistory searchHistory = SearchHistory.builder()
                .userId(userId)
                .keyword(keyword.trim())
                .searchTime(new Date())
                .build();
            
            Boolean result = shopService.saveSearchHistory(searchHistory);
            
            if (result) {
                log.info("手动添加搜索历史成功");
                return Result.success(true);
            } else {
                log.warn("手动添加搜索历史失败");
                return Result.error("添加搜索历史失败");
            }
        } catch (Exception e) {
            log.error("添加搜索历史时发生错误: ", e);
            
            // 特殊处理外键约束错误
            if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                return Result.error("用户ID不存在: " + userId);
            }
            
            return Result.error("添加搜索历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定的搜索历史记录
     * 注意：当前ShopService中未实现deleteSearchHistory方法，使用临时实现
     *
     * @param historyId 搜索历史记录ID
     * @return 操作结果
     */
    @DeleteMapping("/history/{historyId}")
    public Result<Boolean> deleteSearchHistory(@PathVariable("historyId") Long historyId) {
        log.info("删除指定的搜索历史记录：historyId={}", historyId);
        
        try {
            // 由于当前ShopService未实现deleteSearchHistory方法，暂时返回错误信息
            log.warn("删除单条搜索历史记录功能尚未实现");
            return Result.error("此功能尚未实现，请使用清空搜索历史功能");
            
            // 以下是未来实现方向，需要在ShopService中添加对应方法
            // Boolean result = shopService.deleteSearchHistory(historyId);
            // if (result) {
            //     log.info("成功删除搜索历史记录[{}]", historyId);
            // } else {
            //     log.warn("删除搜索历史记录[{}]失败", historyId);
            // }
            // return Result.success(result);
        } catch (Exception e) {
            log.error("删除搜索历史记录时发生错误: ", e);
            return Result.error("删除搜索历史记录失败: " + e.getMessage());
        }
    }
} 