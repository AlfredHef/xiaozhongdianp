package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户优惠券数据访问接口
 */
@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
@Select("select * from user_coupon where user_id=#{userId}")
    List<UserCoupon> findByUserId(Long userId);
  @Select("select count(*) from user_coupon where user_id=#{userId} and coupon_id=#{couponId}")
    Integer countByUserIdAndCouponId(Long userId, Long couponId);
}