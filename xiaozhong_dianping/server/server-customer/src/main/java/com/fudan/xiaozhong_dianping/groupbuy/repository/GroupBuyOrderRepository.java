package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyOrder;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 团购订单数据访问接口
 */
@Repository
public interface GroupBuyOrderRepository extends JpaRepository<GroupBuyOrder, Long> {
    
    /**
     * 根据用户ID查询订单，按创建时间倒序排序
     * @param userId 用户ID
     * @return 订单列表
     */
    List<GroupBuyOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据套餐ID和用户ID查询订单
     * @param packageId 套餐ID
     * @param userId 用户ID
     * @return 订单列表
     */
        @Select("select * from group_buy_order where package_id=#{packageId} and user_id=#{userId}")
    List<GroupBuyOrder> findByPackageIdAndUserId(Integer packageId, Long userId);
    
    /**
     * 根据商家ID和用户ID查询订单
     * @param shopId 商家ID
     * @param userId 用户ID
     * @return 订单列表
     */
    @Select("select * from group_buy_order where shop_id=#{shopId} and user_id=#{userId}")
    List<GroupBuyOrder> findByShopIdAndUserId(Integer shopId, Long userId);
} 