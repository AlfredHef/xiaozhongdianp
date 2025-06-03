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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
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
            // 如果提供了邀请码，先验证邀请码有效性（不创建实际订单）
            if (invitationCode != null && !invitationCode.isEmpty()) {
                logger.info("步骤1: 验证邀请码有效性 - 邀请码: {}", invitationCode);
                
                // 验证邀请码是否存在
                InvitationCode code = invitationService.findByCode(invitationCode);
                if (code == null) {
                    logger.error("❌ 邀请码不存在: {}", invitationCode);
                    throw new BusinessException("邀请码不存在");
                }
                
                // 验证不能使用自己的邀请码
                if (code.getUserId().equals(userId)) {
                    logger.error("❌ 用户尝试使用自己的邀请码，用户ID: {}, 邀请码: {}", userId, invitationCode);
                    throw new BusinessException("不能使用自己的邀请码");
                }
                
                // 检查被邀请人是否已经被邀请过
                boolean hasBeenInvited = invitationService.hasBeenInvited(userId);
                if (hasBeenInvited) {
                    logger.error("❌ 用户已经被邀请过，用户ID: {}", userId);
                    throw new BusinessException("您已经被邀请过，不能重复使用邀请码");
                }
                
                logger.info("✅ 邀请码验证通过 - 邀请人ID: {}", code.getUserId());
            }
            
            // 验证通过后，创建普通订单（包含券码生成）
            logger.info("步骤2: 开始创建普通订单 - 用户ID: {}, 套餐ID: {}", userId, packageId);
            VoucherDTO voucherDTO = orderService.createOrder(userId, packageId, couponId);
            logger.info("✅ 订单创建成功 - 订单ID: {}, 券码: {}", voucherDTO.getOrderId(), voucherDTO.getCode());
            
            // 如果提供了邀请码，处理邀请关系
            if (invitationCode != null && !invitationCode.isEmpty()) {
                logger.info("步骤3: 开始处理邀请关系 - 邀请码: {}", invitationCode);
                try {
                    // 查询订单对象
                    logger.info("步骤3.1: 查询订单对象 - 订单ID: {}", voucherDTO.getOrderId());
                    GroupBuyOrder order = orderService.getOrderById(voucherDTO.getOrderId());
                    logger.info("✅ 获取订单成功 - 订单详情: ID={}, 用户ID={}, 金额={}", 
                        order.getId(), order.getUserId(), order.getOrderPrice());
                    
                    // 处理邀请关系
                    logger.info("步骤3.2: 调用邀请服务处理邀请关系");
                    boolean success = invitationService.useInvitationCode(userId, invitationCode, order);
                    if (success) {
                        logger.info("✅ 邀请关系处理成功 - 用户ID: {}, 邀请码: {}", userId, invitationCode);
                    } else {
                        logger.warn("⚠️ 邀请关系处理返回失败 - 用户ID: {}, 邀请码: {}", userId, invitationCode);
                        throw new BusinessException("邀请关系处理失败");
                    }
                } catch (Exception e) {
                    // 邀请处理异常时，需要回滚已创建的订单
                    logger.error("❌ 处理邀请关系异常，需要回滚订单: {}", e.getMessage());
                    logger.error("异常详情: ", e);
                    
                    // 这里可以考虑添加订单回滚逻辑，但为了简化，直接抛出异常
                    // 让事务管理器处理回滚
                    throw new BusinessException("邀请码处理失败: " + e.getMessage());
                }
            } else {
                logger.info("未提供邀请码，跳过邀请关系处理");
            }
            
            logger.info("=== 订单创建流程完成，返回结果 ===");
            return ResponseEntity.ok(voucherDTO);
        } catch (BusinessException e) {
            logger.error("❌ 业务异常: {}", e.getMessage());
            throw e;
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