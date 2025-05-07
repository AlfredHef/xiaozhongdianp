package com.fudan.xiaozhong_dianping.groupbuy.dto;

import com.fudan.xiaozhong_dianping.groupbuy.entity.Coupon;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 优惠券数据传输对象
 */
@Data
public class CouponDTO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
    private boolean isNewUserCoupon;
    private boolean newUserCoupon;
    private BigDecimal discountAmount;
    private LocalDateTime receivedAt;

    // 格式化后的显示信息
    private String formattedExpirationDate;
    private String formattedReceivedDate;
    private String statusText;
    private String shortDescription;
    private String formattedDescription;

    /**
     * 从Coupon实体创建DTO
     */
    public static CouponDTO fromEntity(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setTitle(coupon.getTitle());
        dto.setDescription(coupon.getDescription());
        dto.setType(coupon.getType());
        dto.setAmount(coupon.getAmount());
        dto.setMaxDeduction(coupon.getMaxDeduction());
        dto.setUseThreshold(coupon.getUseThreshold());
        dto.setApplicableCategory(coupon.getApplicableCategory());
        dto.setApplicableShop(coupon.getApplicableShop());
        dto.setExpirationDate(coupon.getExpirationDate());
        dto.setValidDays(coupon.getValidDays());
        dto.setTotalQuantity(coupon.getTotalQuantity());
        dto.setMaxPerUser(coupon.getMaxPerUser());
        dto.setNewUserCoupon(coupon.isNewUserCoupon());
        
        // 设置格式化信息
        if (coupon.getExpirationDate() != null) {
            dto.setFormattedExpirationDate(coupon.getExpirationDate().format(DATE_FORMATTER));
        }
        
        dto.setShortDescription(coupon.getShortDescription());
        dto.setFormattedDescription(coupon.getFormattedDescription());
        
        return dto;
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case 0:
                return "未使用";
            case 1:
                return "已使用";
            case 2:
                return "已过期";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取有效期文本
     */
    public String getValidityText() {
        if (expirationDate != null) {
            return "有效期至：" + formattedExpirationDate;
        } else if (validDays != null) {
            return "领取后" + validDays + "天内有效";
        }
        return "长期有效";
    }

    /**
     * 获取使用门槛文本
     */
    public String getThresholdText() {
        if (useThreshold != null) {
            return "满" + useThreshold + "元可用";
        }
        return "无门槛";
    }

    /**
     * 获取优惠金额文本
     */
    public String getDiscountText() {
        if (amount == null) {
            return "无优惠";
        }
        switch (type) {
            case "减固定金额":
                return "减" + amount + "元";
            case "减到固定金额":
                return "减至" + amount + "元";
            case "折扣券":
                return amount.multiply(new BigDecimal("100")) + "折";
            default:
                return "无优惠";
        }
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
    
    // 设置isNewUserCoupon时同时设置newUserCoupon
    public void setIsNewUserCoupon(boolean isNewUserCoupon) {
        this.isNewUserCoupon = isNewUserCoupon;
        this.newUserCoupon = isNewUserCoupon;
    }
    
    // 设置newUserCoupon时同时设置isNewUserCoupon
    public void setNewUserCoupon(boolean newUserCoupon) {
        this.newUserCoupon = newUserCoupon;
        this.isNewUserCoupon = newUserCoupon;
    }
}