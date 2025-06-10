package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 团购套餐数据访问接口
 */
@Repository
public interface GroupBuyPackageRepository extends JpaRepository<GroupBuyPackage, Integer> {
    
    /**
     * 根据商家ID查询团购套餐列表
     * @param shopId 商家ID
     * @return 团购套餐列表
     */
    @Select("select * from group_buying_package where shop_id=#{shopId}")
    List<GroupBuyPackage> findByShopId(Integer shopId);
} 