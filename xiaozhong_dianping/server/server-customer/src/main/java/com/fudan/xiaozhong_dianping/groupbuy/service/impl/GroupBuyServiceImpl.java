package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.dto.GroupBuyPackageDTO;
import com.fudan.xiaozhong_dianping.groupbuy.dto.PackageDishItemDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.PackageDishRelation;
import com.fudan.xiaozhong_dianping.groupbuy.exception.BusinessException;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyOrderRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.PackageDishRelationRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.GroupBuyService;
import com.fudan.xiaozhong_dianping.groupbuy.service.OrderService;
import com.fudan.xiaozhong_dianping.invitation.service.InvitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 团购套餐服务实现类
 */
@Service
public class GroupBuyServiceImpl implements GroupBuyService {

    private static final Logger logger = LoggerFactory.getLogger(GroupBuyServiceImpl.class);

    @Autowired
    private GroupBuyPackageRepository packageRepository;
    
    @Autowired
    private PackageDishRelationRepository relationRepository;
    
    @Autowired
    private GroupBuyOrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;

    @Override
    public List<GroupBuyPackageDTO> getPackagesByShopId(Integer shopId) {
        logger.info("开始查询商家团购套餐，商家ID: {}", shopId);
        try {
            // 查询商家的套餐列表
            logger.info("正在调用packageRepository.findByShopId，商家ID: {}", shopId);
            List<GroupBuyPackage> packages = packageRepository.findByShopId(shopId);
            logger.info("查询到团购套餐数量: {}", packages != null ? packages.size() : 0);
            
            if (packages == null) {
                logger.error("查询结果为空，商家ID: {}", shopId);
                return new ArrayList<>();
            }
            
            // 转换为DTO
            logger.info("开始转换DTO，原始数据: {}", packages);
            List<GroupBuyPackageDTO> result = packages.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("转换后的DTO数量: {}", result.size());
            logger.info("转换后的DTO数据: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("查询团购套餐失败，商家ID: {}，错误信息: {}", shopId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public GroupBuyPackageDTO getPackageDetailById(Integer packageId) {
        logger.info("开始查询团购套餐详情，套餐ID: {}", packageId);
        
        // 查询套餐详情
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(packageId);
        if (!packageOpt.isPresent()) {
            logger.warn("未找到团购套餐，套餐ID: {}", packageId);
            return null;
        }
        
        GroupBuyPackage groupBuyPackage = packageOpt.get();
        GroupBuyPackageDTO dto = convertToDTO(groupBuyPackage);
        logger.info("查询到团购套餐基本信息: {}", dto);
        
        // 查询套餐菜品关系
        try {
            logger.info("开始查询套餐菜品关系，套餐ID: {}", packageId);
            List<PackageDishRelation> relations = relationRepository.findByGroupBuyPackageId(packageId);
            logger.info("查询到套餐菜品关系数量: {}", relations != null ? relations.size() : 0);
            
            // 转换为DTO中的菜品列表
            List<PackageDishItemDTO> dishItems = relations.stream()
                    .map(this::convertToDishItemDTO)
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
            logger.info("转换后的菜品列表数量: {}", dishItems.size());
            logger.info("菜品列表详情: {}", dishItems);
            
            dto.setDishItems(dishItems);
        } catch (Exception e) {
            logger.error("查询套餐菜品关系失败，套餐ID: {}，错误信息: {}", packageId, e.getMessage(), e);
            // 设置一个空列表，防止前端获取到null
            dto.setDishItems(new ArrayList<>());
        }
        
        return dto;
    }

    // 在现有的GroupBuyOrderServiceImpl类中添加以下代码
    @Autowired
    private InvitationService invitationService;

    /**
     * 创建基本订单
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param couponId 优惠券ID
     * @return 创建的订单
     */
    @Transactional
    public GroupBuyOrder createOrder(Long userId, Integer packageId, Long couponId) {
        // 查询套餐信息
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(packageId);
        if (!packageOpt.isPresent()) {
            throw new BusinessException("套餐不存在");
        }

        GroupBuyPackage groupBuyPackage = packageOpt.get();
        
        // 创建订单
        GroupBuyOrder order = new GroupBuyOrder();
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setShopId(groupBuyPackage.getShopId());
        order.setOrderPrice(groupBuyPackage.getPrice());
        order.setStatus(1); // 已购买
        order.setCreatedAt(LocalDateTime.now());
        
        // 保存订单
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public GroupBuyOrder createOrderWithInvitation(Long userId, Integer packageId, Long couponId, String invitationCode) {
        // 创建基本订单
        GroupBuyOrder order = createOrder(userId, packageId, couponId);

        // 如果提供了邀请码，则处理邀请关系
        if (invitationCode != null && !invitationCode.isEmpty()) {
            try {
                invitationService.useInvitationCode(userId, invitationCode, order);
            } catch (BusinessException e) {
                // 邀请码使用失败不影响订单创建，只记录日志
                logger.error("使用邀请码失败: {}", e.getMessage());
            }
        }

        return order;
    }
    
    /**
     * 将实体转换为DTO
     */
    private GroupBuyPackageDTO convertToDTO(GroupBuyPackage source) {
        if (source == null) {
            return null;
        }
        
        GroupBuyPackageDTO target = new GroupBuyPackageDTO();
        BeanUtils.copyProperties(source, target);
        target.setDishItems(new ArrayList<>());
        return target;
    }
    
    /**
     * 将套餐菜品关系转换为菜品项DTO
     */
    private PackageDishItemDTO convertToDishItemDTO(PackageDishRelation relation) {
        if (relation == null || relation.getDish() == null) {
            return null;
        }
        
        PackageDishItemDTO dto = new PackageDishItemDTO();
        dto.setDishId(relation.getDish().getId());
        dto.setDishName(relation.getDish().getName());
        dto.setDishPrice(relation.getDish().getPrice());
        dto.setDishDescription(relation.getDish().getDescription());
        dto.setQuantity(relation.getQuantity());
        return dto;
    }
} 