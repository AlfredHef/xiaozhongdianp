package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
   @Select("select * from coupon where is_new_coupon=true")
    List<Coupon> findAllByIsNewUserCouponTrue(); // 新增：查询所有新人券
    @Select("select * from coupon where total_quantity &lt; #{totalQuantity}")
    List<Coupon> findByTotalQuantityGreaterThan(int totalQuantity); // 原有方法保留
}