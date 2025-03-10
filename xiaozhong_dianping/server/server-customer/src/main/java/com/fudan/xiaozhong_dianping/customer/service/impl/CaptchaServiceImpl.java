package com.fudan.xiaozhong_dianping.customer.service.impl;
//import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class CaptchaServiceImpl implements CaptchaService {
//    private final RedisTemplate<String, String> redisTemplate;
//
//    public CaptchaServiceImpl(RedisTemplate<String, String> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    @Override
//    public boolean validateCaptcha(String userInputCaptcha) {
//        // 假设验证码存储在Redis，键为固定值（实际需结合业务生成唯一键）
//        String storedCaptcha = redisTemplate.opsForValue().get("captcha_key");
//        if (storedCaptcha == null) {
//            return false;
//        }
//        boolean isMatch = storedCaptcha.equalsIgnoreCase(userInputCaptcha); // 忽略大小写对比
//        redisTemplate.delete("captcha_key"); // 验证后删除，防止重复使用
//        return isMatch;
//    }
//}
import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    private static final Map<String, String> captchaStore = new ConcurrentHashMap<>();

    @Override
    public boolean validateCaptcha(String userInputCaptcha) {
        String storedCaptcha = captchaStore.get("captcha_key");
        if (storedCaptcha == null) {
            return false;
        }
        boolean isMatch = storedCaptcha.equalsIgnoreCase(userInputCaptcha);
        captchaStore.remove("captcha_key"); // 验证后删除
        return isMatch;
    }

    public void saveCaptcha(String captcha) {
        captchaStore.put("captcha_key", captcha);
        // 可以加个定时任务删除验证码，模拟 Redis 过期机制
    }
}
