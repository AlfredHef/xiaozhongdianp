package com.fudan.xiaozhong_dianping.groupbuy.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订单创建成功事件
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final Long orderId;
    private final Long userId;
    private final Integer packageId;
    private final Long couponId;

    public OrderCreatedEvent(Object source, Long orderId, Long userId, Integer packageId, Long couponId) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.packageId = packageId;
        this.couponId = couponId;
    }
}