package com.fudan.xiaozhong_dianping.groupbuy.listener;

import com.fudan.xiaozhong_dianping.groupbuy.event.OrderCreatedEvent;
import com.fudan.xiaozhong_dianping.groupbuy.entity.GroupBuyPackage;
import com.fudan.xiaozhong_dianping.groupbuy.repository.GroupBuyPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 销量统计事件监听器
 */
@Component
public class SalesStatisticsListener {

    @Autowired
    private GroupBuyPackageRepository packageRepository;

    /**
     * 监听订单创建事件，更新套餐销量
     */
    @EventListener
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        Integer packageId = event.getPackageId();

        Optional<GroupBuyPackage> packageOpt = packageRepository.findById(packageId);
        if (packageOpt.isPresent()) {
            GroupBuyPackage groupBuyPackage = packageOpt.get();
            groupBuyPackage.increaseSales();
            packageRepository.save(groupBuyPackage);
        }
    }
}