package com.fudan.xiaozhong_dianping.groupbuy.service;

import com.fudan.xiaozhong_dianping.groupbuy.dto.OrderDTO;
import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param couponId 优惠券ID，可为null表示不使用优惠券
     * @return 订单DTO，包含券码信息
     */
    VoucherDTO createOrder(Long userId, Integer packageId, Long couponId);
    
    /**
     * 获取用户所有订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<OrderDTO> getOrdersByUserId(Long userId);
    
    /**
     * 根据ID获取订单详情
     * @param orderId 订单ID
     * @return 订单DTO，包含券码信息
     */
    VoucherDTO getOrderDetail(Long orderId);
} 