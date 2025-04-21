package com.fudan.xiaozhong_dianping.groupbuy.controller;

import com.fudan.xiaozhong_dianping.groupbuy.dto.GroupBuyPackageDTO;
import com.fudan.xiaozhong_dianping.groupbuy.service.GroupBuyService;
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
    
    @Autowired
    private GroupBuyService groupBuyService;
    
    /**
     * 获取商家的团购套餐列表
     * @param shopId 商家ID
     * @return 团购套餐列表
     */
    @GetMapping("/packages")
    public ResponseEntity<List<GroupBuyPackageDTO>> getPackagesByShopId(@RequestParam Integer shopId) {
        List<GroupBuyPackageDTO> packages = groupBuyService.getPackagesByShopId(shopId);
        return ResponseEntity.ok(packages);
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