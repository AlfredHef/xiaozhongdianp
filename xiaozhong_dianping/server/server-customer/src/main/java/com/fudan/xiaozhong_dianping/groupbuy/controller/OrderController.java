package com.fudan.xiaozhong_dianping.groupbuy.controller;

import com.fudan.xiaozhong_dianping.groupbuy.dto.OrderDTO;
import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.service.GroupBuyService;
import com.fudan.xiaozhong_dianping.groupbuy.service.OrderService;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationCode;
import com.fudan.xiaozhong_dianping.invitation.service.InvitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private GroupBuyService groupBuyService;
    
    @Autowired
    private InvitationService invitationService;

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
        if (userId == null) {
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
     * 创建订单并使用邀请码
     * @param userId 当前登录用户ID
     * @param requestBody 请求体，包含packageId、couponId和invitationCode
     * @return 订单信息和券码
     */
    @PostMapping("/with-invitation")
    public ResponseEntity<VoucherDTO> createOrderWithInvitation(
            @RequestHeader("userId") Long userId,
            @RequestBody Map<String, Object> requestBody) {
        if (userId == null) {
            throw new BusinessException("用户未登录，请先登录");
        }
        
        Integer packageId = (Integer) requestBody.get("packageId");
        Long couponId = requestBody.get("couponId") != null
                ? Long.valueOf(requestBody.get("couponId").toString())
                : null;
        String invitationCode = (String) requestBody.get("invitationCode");
        
        logger.info("=== 开始处理带邀请码的订单创建请求 ===");
        logger.info("用户ID: {}, 套餐ID: {}, 优惠券ID: {}, 邀请码: {}", userId, packageId, couponId, invitationCode);
        
        try {
            // 创建普通订单（包含券码生成）
            logger.info("步骤1: 开始创建普通订单 - 用户ID: {}, 套餐ID: {}", userId, packageId);
            VoucherDTO voucherDTO = orderService.createOrder(userId, packageId, couponId);
            logger.info("✅ 订单创建成功 - 订单ID: {}, 券码: {}", voucherDTO.getOrderId(), voucherDTO.getCode());
            
            // 如果提供了邀请码，处理邀请关系
            if (invitationCode != null && !invitationCode.isEmpty()) {
                logger.info("步骤2: 开始处理邀请关系 - 邀请码: {}", invitationCode);
                try {
                    // 查询订单对象
                    logger.info("步骤2.1: 查询订单对象 - 订单ID: {}", voucherDTO.getOrderId());
                    GroupBuyOrder order = orderService.getOrderById(voucherDTO.getOrderId());
                    logger.info("✅ 获取订单成功 - 订单详情: ID={}, 用户ID={}, 金额={}", 
                        order.getId(), order.getUserId(), order.getOrderPrice());
                    
                    // 处理邀请关系
                    logger.info("步骤2.2: 调用邀请服务处理邀请关系");
                    boolean success = invitationService.useInvitationCode(userId, invitationCode, order);
                    if (success) {
                        logger.info("✅ 邀请关系处理成功 - 用户ID: {}, 邀请码: {}", userId, invitationCode);
                    } else {
                        logger.warn("⚠️ 邀请关系处理返回失败，但不影响订单创建 - 用户ID: {}, 邀请码: {}", userId, invitationCode);
                    }
                } catch (Exception e) {
                    // 记录邀请处理异常，但不影响订单创建
                    logger.error("❌ 处理邀请关系异常，但不影响订单创建");
                    logger.error("异常详情: {}", e.getMessage());
                    logger.error("异常类型: {}", e.getClass().getSimpleName());
                    if (e.getCause() != null) {
                        logger.error("异常原因: {}", e.getCause().getMessage());
                    }
                    logger.error("异常堆栈:", e);
                    
                    // 重要：这里记录具体的异常信息，这样我们就能看到真正的错误原因
                    System.out.println("=== OrderController 捕获到邀请处理异常 ===");
                    System.out.println("用户ID: " + userId);
                    System.out.println("邀请码: " + invitationCode);
                    System.out.println("异常消息: " + e.getMessage());
                    System.out.println("异常类型: " + e.getClass().getName());
                    System.out.println("=== 异常处理完毕，继续返回订单结果 ===");
                }
            } else {
                logger.info("未提供邀请码，跳过邀请关系处理");
            }
            
            logger.info("=== 订单创建流程完成，返回结果 ===");
            return ResponseEntity.ok(voucherDTO);
        } catch (Exception e) {
            logger.error("❌ 创建订单失败: {}", e.getMessage(), e);
            throw new BusinessException("创建订单失败: " + e.getMessage());
        }
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