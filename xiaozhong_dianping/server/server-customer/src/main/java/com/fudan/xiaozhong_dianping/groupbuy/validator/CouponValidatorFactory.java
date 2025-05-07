package com.fudan.xiaozhong_dianping.groupbuy.validator;

import com.fudan.xiaozhong_dianping.groupbuy.validator.impl.*;
import org.springframework.stereotype.Component;

/**
 * 优惠券校验器工厂
 */
@Component
public class CouponValidatorFactory {
    
    /**
     * 创建默认的校验器链
     * @return 校验器链
     */
    public CouponValidator createDefaultValidatorChain() {
        // 创建校验器
        ExpirationValidator expirationValidator = new ExpirationValidator();
        ThresholdValidator thresholdValidator = new ThresholdValidator();
        UsageStatusValidator usageStatusValidator = new UsageStatusValidator();
        ApplicabilityValidator applicabilityValidator = new ApplicabilityValidator();
        
        // 构建校验器链
        expirationValidator
            .setNext(thresholdValidator)
            .setNext(usageStatusValidator)
            .setNext(applicabilityValidator);
        
        return expirationValidator;
    }
    
    /**
     * 创建自定义的校验器链
     * @param validators 校验器数组
     * @return 校验器链
     */
    public CouponValidator createCustomValidatorChain(CouponValidator... validators) {
        if (validators == null || validators.length == 0) {
            return null;
        }
        
        // 构建校验器链
        for (int i = 0; i < validators.length - 1; i++) {
            validators[i].setNext(validators[i + 1]);
        }
        
        return validators[0];
    }
} 