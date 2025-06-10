package com.fudan.xiaozhong_dianping.groupbuy.controller;

import com.fudan.xiaozhong_dianping.groupbuy.dto.VoucherDTO;
import com.fudan.xiaozhong_dianping.groupbuy.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 券码相关接口
 */
@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    /**
     * 根据券码获取详情
     * @param code 券码
     * @return 券码详情
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<VoucherDTO> getVoucherByCode(@PathVariable String code) {
        VoucherDTO voucherDTO = voucherService.getVoucherByCode(code);
        if (voucherDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(voucherDTO);
    }
} 