package com.fudan.xiaozhong_dianping.groupbuy.controller;

import com.fudan.xiaozhong_dianping.groupbuy.dto.GroupBuyPackageDTO;
import com.fudan.xiaozhong_dianping.groupbuy.service.GroupBuyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团购套餐相关接口
 */
@RestController
@RequestMapping("/api/groupbuy")
public class GroupBuyController {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupBuyController.class);
    
    @Autowired
    private GroupBuyService groupBuyService;
    
    /**
     * 获取商家的团购套餐列表
     * @param shopId 商家ID
     * @return 团购套餐列表
     */
    @GetMapping("/packages")
    public ResponseEntity<List<GroupBuyPackageDTO>> getPackagesByShopId(@RequestParam Integer shopId) {
        logger.info("收到获取团购套餐请求，商家ID: {}", shopId);
        try {
            logger.info("开始调用groupBuyService.getPackagesByShopId");
            List<GroupBuyPackageDTO> packages = groupBuyService.getPackagesByShopId(shopId);
            logger.info("成功获取团购套餐，数量: {}", packages != null ? packages.size() : 0);
            logger.info("团购套餐数据: {}", packages);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            logger.error("获取团购套餐失败，商家ID: {}，错误信息: {}", shopId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取团购套餐详情
     * @param packageId 套餐ID
     * @return 套餐详情，包含菜品信息
     */
    @GetMapping("/packages/{packageId}")
    public ResponseEntity<GroupBuyPackageDTO> getPackageDetail(@PathVariable Integer packageId) {
        GroupBuyPackageDTO packageDetail = groupBuyService.getPackageDetailById(packageId);
        if (packageDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(packageDetail);
    }
} 