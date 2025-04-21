package com.fudan.xiaozhong_dianping.groupbuy.service;

import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;

/**
 * 券码服务接口
 */
public interface VoucherService {
    
    /**
     * 根据券码查询券码详情
     * @param code 券码
     * @return 券码信息，包含相关订单和套餐信息
     */
    VoucherDTO getVoucherByCode(String code);
} 