package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Dish;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜品数据访问接口
 */
@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {
    
    /**
     * 根据商家ID查询菜品列表
     * @param shopId 商家ID
     * @return 菜品列表
     */
    @Select("select * from dish where shop_id=#{shopId}")
    List<Dish> findByShopId(Integer shopId);
} 