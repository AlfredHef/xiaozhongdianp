package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.CouponCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponCategoryRepository extends JpaRepository<CouponCategory, Long> {
    
    /**
     * 根据优惠券ID查询关联的品类
     */
    List<CouponCategory> findByCouponId(Long couponId);
    
    /**
     * 根据品类ID查询关联的优惠券
     */
    List<CouponCategory> findByCategoryId(Integer categoryId);
    
    /**
     * 根据优惠券ID和品类ID查询关联
     */
    CouponCategory findByCouponIdAndCategoryId(Long couponId, Integer categoryId);
} 