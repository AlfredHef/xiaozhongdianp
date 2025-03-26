package com.fudan.xiaozhong_dianping.customer.service.impl;

import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 验证码服务实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final DefaultKaptcha captchaProducer;
    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceImpl.class);
    private final RedisTemplate redisTemplate;
    /**
     * 依赖注入 Kaptcha
     */
    @Autowired
    public CaptchaServiceImpl(DefaultKaptcha captchaProducer, RedisTemplate redisTemplate) {
        this.captchaProducer = captchaProducer;
        this.redisTemplate = redisTemplate;

    }

    /**
     * 生成验证码并返回验证码 ID
     */
    @Override
    public String generateCaptcha() {
        try {
            // 生成验证码文本
            String captchaText = captchaProducer.createText();
            String captchaId = UUID.randomUUID().toString();
            // 存储到Redis，键为"captcha:{captchaId}"，有效期5分钟
            redisTemplate.opsForValue().set("captcha:" + captchaId, captchaText, 5, TimeUnit.MINUTES);
            return captchaId;
        } catch (Exception e) {
            logger.error("Failed to generate captcha", e);
            return null;
        }
    }

    /**
     * 获取验证码图片
     */
    @Override
    public BufferedImage getCaptchaImage(String captchaId) {
        try {
            // 从 Redis 获取验证码文本
            String captchaText = (String) redisTemplate.opsForValue().get("captcha:" + captchaId);
            if (captchaText == null) {
                return null;
            }
            return captchaProducer.createImage(captchaText);
        } catch (Exception e) {
            logger.error("Failed to get captcha image for captchaId: {}", captchaId, e);
            return null;
        }
    }

    /**
     * 校验验证码
     */
    @Override
    public boolean validateCaptcha(String captchaId, String captchaText) {

        System.out.println("Captcha ID received: " + captchaId);  // 打印验证码 ID
        System.out.println("Captcha Text received: " + captchaText);  // 打印验证码文本

        String storedCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + captchaId);
        if (storedCaptcha == null) {
            System.out.println("Captcha ID not found in store");
            return false;  // 如果没有找到验证码 ID
        }



        boolean isValid = storedCaptcha.equalsIgnoreCase(captchaText);

//        // 如果校验成功，删除验证码
//        if (isValid) {
//            redisTemplate.delete("captcha:" + captchaId); // 校验成功后删除
//            System.out.println("Captcha is valid, removing from store");
//        } else {
//            System.out.println("Captcha is invalid");
//        }

        return isValid;
    }


}


