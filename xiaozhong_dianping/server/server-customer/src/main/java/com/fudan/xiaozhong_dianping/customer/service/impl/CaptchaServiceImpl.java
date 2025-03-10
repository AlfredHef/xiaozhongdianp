package com.fudan.xiaozhong_dianping.customer.service.impl;
import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    private final RedisTemplate<String, String> redisTemplate;

    public CaptchaServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean validateCaptcha(String userInputCaptcha) {
        // 假设验证码存储在Redis，键为固定值（实际需结合业务生成唯一键）
        String storedCaptcha = redisTemplate.opsForValue().get("captcha_key");
        if (storedCaptcha == null) {
            return false;
        }
        boolean isMatch = storedCaptcha.equalsIgnoreCase(userInputCaptcha); // 忽略大小写对比
        redisTemplate.delete("captcha_key"); // 验证后删除，防止重复使用
        return isMatch;
    }
}
