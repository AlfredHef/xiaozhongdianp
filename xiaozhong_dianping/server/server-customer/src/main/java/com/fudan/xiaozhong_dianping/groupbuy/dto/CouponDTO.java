package com.fudan.xiaozhong_dianping.groupbuy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券数据传输对象
 */
@Data
public class CouponDTO {

    private Long id;
    private String title;
    private String description;
    private String type;
    private BigDecimal amount;
    private BigDecimal maxDeduction;
    private BigDecimal useThreshold;
    private String applicableCategory;
    private String applicableShop;
    private LocalDateTime expirationDate;
    private Integer validDays;
    private Integer totalQuantity;
    private Integer maxPerUser;
    private Integer status; // 0-未使用，1-已使用，2-已过期
    private boolean isNewUserCoupon; // 是否为新人券
}