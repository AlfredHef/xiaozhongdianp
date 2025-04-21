package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.dto.OrderDTO;
import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.VoucherCode;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyOrderRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.VoucherCodeRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.OrderService;
import com.fudan.xiaozhong_dianping.groupbuy.utils.QRCodeGenerator;
import com.fudan.xiaozhong_dianping.groupbuy.utils.VoucherCodeGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private GroupBuyOrderRepository orderRepository;
    
    @Autowired
    private GroupBuyPackageRepository packageRepository;
    
    @Autowired
    private VoucherCodeRepository voucherCodeRepository;
    
    @Autowired
    private QRCodeGenerator qrCodeGenerator;
    
    @Autowired
    private VoucherCodeGenerator voucherCodeGenerator;

    @Override
    @Transactional
    public VoucherDTO createOrder(Long userId, Integer packageId, Long couponId) {
        // 查询套餐
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(packageId);
        if (!packageOpt.isPresent()) {
            throw new RuntimeException("套餐不存在");
        }
        
        GroupBuyPackage groupBuyPackage = packageOpt.get();
        
        // 创建订单
        GroupBuyOrder order = new GroupBuyOrder();
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setShopId(groupBuyPackage.getShopId());
        
        // 计算订单价格（这里简化处理，没有考虑优惠券）
        BigDecimal orderPrice = groupBuyPackage.getPrice();
        // 如果有优惠券，这里应该计算优惠后的价格
        
        order.setOrderPrice(orderPrice);
        order.setStatus(1); // 已购买
        order.setCreatedAt(LocalDateTime.now());
        
        // 保存订单
        GroupBuyOrder savedOrder = orderRepository.save(order);
        
        // 更新套餐销量
        groupBuyPackage.increaseSales();
        packageRepository.save(groupBuyPackage);
        
        // 生成券码
        VoucherCode voucherCode = new VoucherCode();
        voucherCode.setOrderId(savedOrder.getId());
        // 使用工具类生成券码
        String code = voucherCodeGenerator.generateVoucherCode();
        voucherCode.setCode(code);
        
        // 生成二维码
        String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(voucherCode.getCode());
        voucherCode.setQrCodeUrl(qrCodeBase64);
        
        // 保存券码
        VoucherCode savedVoucherCode = voucherCodeRepository.save(voucherCode);
        
        // 转为DTO返回
        return convertToVoucherDTO(savedVoucherCode, savedOrder, groupBuyPackage);
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<GroupBuyOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherDTO getOrderDetail(Long orderId) {
        Optional<GroupBuyOrder> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("订单不存在");
        }
        
        GroupBuyOrder order = orderOpt.get();
        
        // 获取券码
        VoucherCode voucherCode = voucherCodeRepository.findByOrderId(order.getId());
        if (voucherCode == null) {
            throw new RuntimeException("券码不存在");
        }
        
        // 获取套餐
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(order.getPackageId());
        if (!packageOpt.isPresent()) {
            throw new RuntimeException("套餐不存在");
        }
        
        GroupBuyPackage groupBuyPackage = packageOpt.get();
        
        // 转为DTO返回
        return convertToVoucherDTO(voucherCode, order, groupBuyPackage);
    }
    
    /**
     * 将订单实体转为DTO
     */
    private OrderDTO convertToOrderDTO(GroupBuyOrder order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        // 查询套餐名称
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(order.getPackageId());
        if (packageOpt.isPresent()) {
            dto.setPackageTitle(packageOpt.get().getTitle());
        }
        
        return dto;
    }
    
    /**
     * 将券码和订单信息转为VoucherDTO
     */
    private VoucherDTO convertToVoucherDTO(VoucherCode voucherCode, GroupBuyOrder order, GroupBuyPackage groupBuyPackage) {
        VoucherDTO dto = new VoucherDTO();
        
        BeanUtils.copyProperties(voucherCode, dto);
        dto.setOrderId(order.getId());
        dto.setOrderPrice(order.getOrderPrice());
        dto.setCreatedTime(order.getCreatedAt());
        dto.setPackageId(groupBuyPackage.getId());
        dto.setPackageTitle(groupBuyPackage.getTitle());
        dto.setShopId(groupBuyPackage.getShopId());
        
        return dto;
    }
} 