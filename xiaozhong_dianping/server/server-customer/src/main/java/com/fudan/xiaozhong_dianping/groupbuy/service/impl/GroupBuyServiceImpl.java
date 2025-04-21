package com.fudan.xiaozhong_dianping.groupbuy.service.impl;

import com.fudan.xiaozhong_dianping.groupbuy.dto.GroupBuyPackageDTO;
import com.fudan.xiaozhong_dianping.groupbuy.dto.PackageDishItemDTO;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.entity.PackageDishRelation;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import com.fudan.xiaozhong_dianping.groupbuy.repository.PackageDishRelationRepository;
import com.fudan.xiaozhong_dianping.groupbuy.service.GroupBuyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 团购套餐服务实现类
 */
@Service
public class GroupBuyServiceImpl implements GroupBuyService {

    @Autowired
    private GroupBuyPackageRepository packageRepository;
    
    @Autowired
    private PackageDishRelationRepository relationRepository;

    @Override
    public List<GroupBuyPackageDTO> getPackagesByShopId(Integer shopId) {
        // 查询商家的套餐列表
        List<GroupBuyPackage> packages = packageRepository.findByShopId(shopId);
        
        // 转换为DTO
        return packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GroupBuyPackageDTO getPackageDetailById(Integer packageId) {
        // 查询套餐详情
        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(packageId);
        if (!packageOpt.isPresent()) {
            return null;
        }
        
        GroupBuyPackage groupBuyPackage = packageOpt.get();
        GroupBuyPackageDTO dto = convertToDTO(groupBuyPackage);
        
        // 查询套餐菜品关系
        List<PackageDishRelation> relations = relationRepository.findByGroupBuyPackageId(packageId);
        
        // 转换为DTO中的菜品列表
        List<PackageDishItemDTO> dishItems = relations.stream()
                .map(this::convertToDishItemDTO)
                .collect(Collectors.toList());
        
        dto.setDishItems(dishItems);
        return dto;
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