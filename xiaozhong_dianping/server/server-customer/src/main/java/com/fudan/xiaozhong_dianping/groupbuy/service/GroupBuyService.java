package com.fudan.xiaozhong_dianping.groupbuy.service;

import com.fudan.xiaozhong_dianping.groupbuy.dto.GroupBuyPackageDTO;

import java.util.List;

/**
 * 团购套餐服务接口
 */
public interface GroupBuyService {
    
    /**
     * 根据商家ID获取团购套餐列表
     * @param shopId 商家ID
     * @return 团购套餐列表
     */
    List<GroupBuyPackageDTO> getPackagesByShopId(Integer shopId);
    
    /**
     * 根据套餐ID获取套餐详情
     * @param packageId 套餐ID
     * @return 套餐详情，包含菜品信息
     */
    GroupBuyPackageDTO getPackageDetailById(Integer packageId);
} 