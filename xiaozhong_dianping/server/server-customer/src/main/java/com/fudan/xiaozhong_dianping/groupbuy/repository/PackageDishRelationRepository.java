package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.PackageDishRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 套餐菜品关系数据访问接口
 */
@Repository
public interface PackageDishRelationRepository extends JpaRepository<PackageDishRelation, Integer> {
    
    /**
     * 根据套餐ID查询套餐菜品关系列表
     * @param packageId 套餐ID
     * @return 套餐菜品关系列表
     */
    List<PackageDishRelation> findByGroupBuyPackageId(Integer packageId);
} 