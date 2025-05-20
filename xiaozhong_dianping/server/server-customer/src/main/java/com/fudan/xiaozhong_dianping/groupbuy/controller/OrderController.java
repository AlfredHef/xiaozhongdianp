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
        
        logger.info("开始处理带邀请码的订单创建请求 - 用户ID: {}, 套餐ID: {}, 邀请码: {}", userId, packageId, invitationCode);
        
        // 先验证邀请码有效性，避免用户使用无效邀请码下单
        if (invitationCode != null && !invitationCode.isEmpty()) {
            try {
                InvitationCode code = invitationService.findByCode(invitationCode);
                if (code == null) {
                    throw new BusinessException("邀请码不存在");
                }
                
                // 验证不能使用自己的邀请码
                if (code.getUserId().equals(userId)) {
                    throw new BusinessException("不能使用自己的邀请码");
                }
                
                // 检查用户是否已被邀请过
                if (invitationService.hasBeenInvited(userId)) {
                    throw new BusinessException("您已经被邀请过，不能重复使用邀请码");
                }
                
                logger.info("邀请码验证通过 - 邀请码: {}, 邀请人ID: {}", invitationCode, code.getUserId());
            } catch (BusinessException e) {
                logger.error("邀请码验证失败: {}", e.getMessage());
                throw e;
            }
        }
        
        try {
            // 创建普通订单（包含券码生成）
            logger.info("开始创建普通订单 - 用户ID: {}, 套餐ID: {}", userId, packageId);
            VoucherDTO voucherDTO = orderService.createOrder(userId, packageId, couponId);
            logger.info("订单创建成功 - 订单ID: {}", voucherDTO.getOrderId());
            
            // 如果提供了邀请码，处理邀请关系
            if (invitationCode != null && !invitationCode.isEmpty()) {
                try {
                    // 查询订单对象
                    GroupBuyOrder order = orderService.getOrderById(voucherDTO.getOrderId());
                    logger.info("获取订单成功，准备处理邀请关系 - 订单ID: {}", order.getId());
                    
                    // 处理邀请关系
                    boolean success = invitationService.useInvitationCode(userId, invitationCode, order);
                    if (success) {
                        logger.info("邀请关系处理成功 - 用户ID: {}, 邀请码: {}", userId, invitationCode);
                    } else {
                        logger.warn("邀请关系处理返回失败，但不影响订单创建 - 用户ID: {}, 邀请码: {}", userId, invitationCode);
                    }
                } catch (Exception e) {
                    // 记录邀请处理异常，但不影响订单创建
                    logger.error("处理邀请关系异常，但不影响订单创建: {}", e.getMessage(), e);
                }
            }
            
            return ResponseEntity.ok(voucherDTO);
        } catch (Exception e) {
            logger.error("创建订单失败: {}", e.getMessage(), e);
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