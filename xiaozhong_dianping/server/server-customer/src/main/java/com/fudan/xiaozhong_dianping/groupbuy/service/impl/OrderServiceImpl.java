package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.dto.OrderDTO;
import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.VoucherCode;

import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyOrderRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.VoucherCodeRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.CouponService;
import com.fudan.xiaozhong_dianping.groupbuy.service.OrderService;
import com.fudan.xiaozhong_dianping.groupbuy.utils.QRCodeGenerator;
import com.fudan.xiaozhong_dianping.groupbuy.utils.VoucherCodeGenerator;
import com.fudan.xiaozhong_dianping.shop.entity.Shop;
import com.fudan.xiaozhong_dianping.shop.mapper.ShopMapper;
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
 * 订单服务实现类，提供订单创建、查询等功能。
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

    @Autowired
    private CouponService couponService;
    
    @Autowired
    private ShopMapper shopMapper;
    
    @Autowired
    private com.fudan.xiaozhong_dianping.groupbuy.repository.UserCouponRepository userCouponRepository;

    /**
     * 创建订单并生成券码。
     *
     * @param userId   用户ID，标识下单用户。
     * @param packageId 套餐ID，标识用户购买的团购套餐。
     * @param couponId  优惠券ID，可为空，标识用户使用的优惠券。
     * @return 返回包含券码信息的VoucherDTO对象。
     * @throws BusinessException 如果套餐不存在或券码生成失败，则抛出业务异常。
     */


    @Override
    @Transactional
    public VoucherDTO createOrder(Long userId, Integer packageId, Long couponId) {
        // 查询套餐信息
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(packageId);
        if (!packageOpt.isPresent()) {
            throw new BusinessException("套餐不存在");
        }

        GroupBuyPackage groupBuyPackage = packageOpt.get();

        // 创建订单对象并设置基本信息
        GroupBuyOrder order = new GroupBuyOrder();
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setShopId(groupBuyPackage.getShopId());

        // 计算订单价格，考虑优惠券折扣
        BigDecimal orderPrice = groupBuyPackage.getPrice();
        if (couponId != null) {
            // 获取用户可用优惠券列表
            List<com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon> availableCoupons = couponService.getAvailableCoupons(userId, packageId, orderPrice);
            // 查找指定优惠券并计算折扣后价格
            com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon selectedCoupon = availableCoupons.stream()
                    .filter(coupon -> coupon.getId().equals(couponId))
                    .findFirst()
                    .orElse(null);
            if (selectedCoupon != null) {
                orderPrice = orderPrice.subtract(couponService.calculateDiscount(selectedCoupon, orderPrice));
                
                // 更新优惠券状态为已使用
                com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon userCoupon = 
                    userCouponRepository.findByUserIdAndCouponId(userId, couponId);
                if (userCoupon != null) {
                    userCoupon.setStatus(1); // 已使用
                    userCoupon.setUsedAt(LocalDateTime.now());
                    userCouponRepository.save(userCoupon);
                }
            }
        }

        // 设置订单价格和状态，并保存订单
        order.setOrderPrice(orderPrice);
        order.setStatus(1); // 已购买
        order.setCreatedAt(LocalDateTime.now());
        GroupBuyOrder savedOrder = orderRepository.save(order);

        // 更新套餐销量
        groupBuyPackage.increaseSales();
        packageRepository.save(groupBuyPackage);

        // 生成券码并保存
        VoucherCode voucherCode = new VoucherCode();
        voucherCode.setOrderId(savedOrder.getId());
        String code = voucherCodeGenerator.generateVoucherCode();
        voucherCode.setCode(code);

        // 生成二维码并关联到券码
        String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(voucherCode.getCode());
        voucherCode.setQrCodeUrl(qrCodeBase64);

        VoucherCode savedVoucherCode = voucherCodeRepository.save(voucherCode);

        // 转换为DTO返回
        return convertToVoucherDTO(savedVoucherCode, savedOrder, groupBuyPackage);
    }

    /**
     * 根据用户ID查询订单列表。
     *
     * @param userId 用户ID，标识查询目标用户。
     * @return 返回包含订单信息的OrderDTO列表，按创建时间倒序排列。
     */
    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<GroupBuyOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据订单ID查询订单详情。
     *
     * @param orderId 订单ID，标识查询目标订单。
     * @return 返回包含订单和券码信息的VoucherDTO对象。
     * @throws BusinessException 如果订单、券码或套餐不存在，则抛出业务异常。
     */
    @Override
    public VoucherDTO getOrderDetail(Long orderId) {
        // 查询订单信息
        Optional<GroupBuyOrder> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new BusinessException("订单不存在");
        }

        GroupBuyOrder order = orderOpt.get();

        // 查询券码信息
        VoucherCode voucherCode = voucherCodeRepository.findByOrderId(order.getId());
        if (voucherCode == null) {
            throw new BusinessException("券码不存在");
        }

        // 查询套餐信息
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(order.getPackageId());
        if (!packageOpt.isPresent()) {
            throw new BusinessException("套餐不存在");
        }

        GroupBuyPackage groupBuyPackage = packageOpt.get();

        // 转换为DTO返回
        return convertToVoucherDTO(voucherCode, order, groupBuyPackage);
    }

    /**
     * 根据ID获取订单实体
     *
     * @param orderId 订单ID
     * @return 订单实体
     * @throws BusinessException 如果订单不存在，则抛出业务异常
     */
    @Override
    public GroupBuyOrder getOrderById(Long orderId) {
        Optional<GroupBuyOrder> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new BusinessException("订单不存在");
        }
        return orderOpt.get();
    }

    /**
     * 将订单实体转换为OrderDTO对象。
     *
     * @param order 订单实体对象。
     * @return 返回转换后的OrderDTO对象。
     */
    private OrderDTO convertToOrderDTO(GroupBuyOrder order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);

        // 查询套餐名称并设置到DTO中
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(order.getPackageId());
        if (packageOpt.isPresent()) {
            dto.setPackageTitle(packageOpt.get().getTitle());
        }
        
        // 查询商家名称
        if (order.getShopId() != null) {
            Shop shop = shopMapper.findShopById(order.getShopId().longValue());
            if (shop != null) {
                dto.setShopName(shop.getName());
            }
        }

        return dto;
    }

    /**
     * 将券码、订单和套餐信息转换为VoucherDTO对象。
     *
     * @param voucherCode 券码实体对象。
     * @param order       订单实体对象。
     * @param groupBuyPackage 套餐实体对象。
     * @return 返回转换后的VoucherDTO对象。
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
        
        // 查询商家名称
        if (groupBuyPackage.getShopId() != null) {
            Shop shop = shopMapper.findShopById(groupBuyPackage.getShopId().longValue());
            if (shop != null) {
                dto.setShopName(shop.getName());
            }
        }

        return dto;
    }
}
