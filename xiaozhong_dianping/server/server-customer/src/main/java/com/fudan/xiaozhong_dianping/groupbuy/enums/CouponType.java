package com.fudan.xiaozhong_dianping.groupbuy.enums;

/**
 * 优惠券类型枚举
 */
public enum CouponType {
    FIXED_AMOUNT("减固定金额", "直接减免固定金额"),
    FIXED_PRICE("减到固定金额", "优惠后价格为固定金额"),
    DISCOUNT("折扣券", "按比例折扣"),
    NEW_USER("新人券", "新用户专享优惠"),
    THRESHOLD("满减券", "满指定金额减免");
    
    private final String displayName;    // 显示名称
    private final String description;    // 详细描述
    
    CouponType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static CouponType fromDisplayName(String displayName) {
        for (CouponType type : values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的优惠券类型: " + displayName);
    }
    
    /**
     * 获取优惠券类型的格式化描述
     */
    public String getFormattedDescription() {
        return String.format("%s - %s", displayName, description);
    }
} 