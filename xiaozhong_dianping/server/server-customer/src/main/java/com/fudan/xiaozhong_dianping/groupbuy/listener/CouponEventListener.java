package com.fudan.xiaozhong_dianping.groupbuy.listener;

import com.fudan.xiaozhong_dianping.groupbuy.event.OrderCreatedEvent;
import com.fudan.xiaozhong_dianping.groupbuy.entity.UserCoupon;
import com.fudan.xiaozhong_dianping.groupbuy.repository.UserCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 优惠券相关事件监听器
 */
@Component
public class CouponEventListener {

    @Autowired
    private UserCouponRepository userCouponRepository;

    /**
     * 监听订单创建事件，更新优惠券状态
     */
    @EventListener
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        Long couponId = event.getCouponId();
        Long userId = event.getUserId();

        // 只有使用了优惠券才需要处理
        if (couponId != null) {
            UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId);
            if (userCoupon != null) {
                userCoupon.setStatus(1); // 已使用
                userCoupon.setUsedAt(LocalDateTime.now());
                userCouponRepository.save(userCoupon);
            }
        }
    }
}