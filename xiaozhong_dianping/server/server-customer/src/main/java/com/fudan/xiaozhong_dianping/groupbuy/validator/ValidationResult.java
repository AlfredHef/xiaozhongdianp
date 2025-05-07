package com.fudan.xiaozhong_dianping.groupbuy.validator;

import lombok.Data;

/**
 * 校验结果类
 */
@Data
public class ValidationResult {
    private boolean valid;
    private String message;
    
    public static ValidationResult success() {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        return result;
    }
    
    public static ValidationResult failure(String message) {
        ValidationResult result = new ValidationResult();
        result.setValid(false);
        result.setMessage(message);
        return result;
    }
} 