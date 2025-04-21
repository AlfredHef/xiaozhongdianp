package com.fudan.xiaozhong_dianping.groupbuy.repository;

import com.fudan.xiaozhong_dianping.groupbuy.entity.VoucherCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 券码数据访问接口
 */
@Repository
public interface VoucherCodeRepository extends JpaRepository<VoucherCode, Long> {
    
    /**
     * 根据订单ID查询券码
     * @param orderId 订单ID
     * @return 券码
     */
    VoucherCode findByOrderId(Long orderId);
    
    /**
     * 根据券码查询
     * @param code 券码
     * @return 券码实体
     */
    VoucherCode findByCode(String code);
} 