package com.fudan.xiaozhong_dianping.groupbuy.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 券码生成工具类
 */
@Component
public class VoucherCodeGenerator {
    
    private static final int CODE_LENGTH = 16;
    private final Random random = new SecureRandom();
    
    /**
     * 生成16位随机数字券码
     * @return 16位数字券码
     */
    public String generateVoucherCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * 生成指定长度的随机数字
     * @param length 长度
     * @return 指定长度的随机数字字符串
     */
    public String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
} 