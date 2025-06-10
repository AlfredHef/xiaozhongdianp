package com.fudan.xiaozhong_dianping.groupbuy.service;

import com.fudan.xiaozhong_dianping.groupbuy.dto.GroupBuyPackageDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;

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

    // 在现有的GroupBuyOrderService接口中添加以下方法
    /**
     * 创建订单并使用邀请码
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param couponId 优惠券ID（可选）
     * @param invitationCode 邀请码（可选）
     * @return 创建的订单
     */
    GroupBuyOrder createOrderWithInvitation(Long userId, Integer packageId, Long couponId, String invitationCode);
} 