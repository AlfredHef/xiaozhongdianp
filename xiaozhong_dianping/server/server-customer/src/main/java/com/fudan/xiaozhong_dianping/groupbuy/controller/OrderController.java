package com.fudan.xiaozhong_dianping.groupbuy.controller;

import com.fudan.xiaozhong_dianping.groupbuy.dto.OrderDTO;
import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单相关接口
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     * @param userId 当前登录用户ID
     * @param requestBody 请求体，包含packageId和couponId
     * @return 订单信息和券码
     */
    @PostMapping
    public ResponseEntity<VoucherDTO> createOrder(
            @RequestHeader("userId") Long userId,
            @RequestBody Map<String, Object> requestBody) {
        if(userId==null){
            throw new BusinessException("用户未登录，请先登录");
        }
        Integer packageId = (Integer) requestBody.get("packageId");
        Long couponId = requestBody.get("couponId") != null 
                ? Long.valueOf(requestBody.get("couponId").toString()) 
                : null;
        
        VoucherDTO voucherDTO = orderService.createOrder(userId, packageId, couponId);
        return ResponseEntity.ok(voucherDTO);
    }
    
    /**
     * 获取用户订单列表
     * @param userId 当前登录用户ID
     * @return 订单列表
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(@RequestHeader("userId") Long userId) {
        if (userId == null) {
            throw new BusinessException("用户未登录，请先登录");

        }
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情，包含券码
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<VoucherDTO> getOrderDetail(@PathVariable Long orderId) {
        VoucherDTO voucherDTO = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(voucherDTO);
    }
} 