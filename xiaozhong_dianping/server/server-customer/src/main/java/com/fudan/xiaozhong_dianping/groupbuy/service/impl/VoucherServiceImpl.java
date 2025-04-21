package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.VoucherCode;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyOrderRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.VoucherCodeRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.VoucherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 券码服务实现
 */
@Service
public class VoucherServiceImpl implements VoucherService {
    
    @Autowired
    private VoucherCodeRepository voucherCodeRepository;
    
    @Autowired
    private GroupBuyOrderRepository orderRepository;
    
    @Autowired
    private GroupBuyPackageRepository packageRepository;

    @Override
    public VoucherDTO getVoucherByCode(String code) {
        // 查询券码
        VoucherCode voucherCode = voucherCodeRepository.findByCode(code);
        if (voucherCode == null) {
            return null;
        }
        
        // 查询关联的订单
        Optional<GroupBuyOrder> orderOpt = orderRepository.findById(voucherCode.getOrderId());
        if (!orderOpt.isPresent()) {
            return null;
        }
        
        GroupBuyOrder order = orderOpt.get();
        
        // 查询套餐信息
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(order.getPackageId());
        if (!packageOpt.isPresent()) {
            return null;
        }
        
        GroupBuyPackage groupBuyPackage = packageOpt.get();
        
        // 转换为DTO
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