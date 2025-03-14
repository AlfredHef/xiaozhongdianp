package com.fudan.xiaozhong_dianping.customer.service.impl;

import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
import com.google.code.kaptcha.impl.DefaultKaptcha;
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
    private final ConcurrentHashMap<String, String> captchaStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DefaultKaptcha captchaProducer;
    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceImpl.class);
    /**
     * 依赖注入 Kaptcha
     */
    @Autowired
    public CaptchaServiceImpl(DefaultKaptcha captchaProducer) {
        this.captchaProducer = captchaProducer;
        // 定期清理过期验证码
        scheduler.scheduleAtFixedRate(this::cleanExpiredCaptchas, 1, 1, TimeUnit.MINUTES);
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
            long timestamp = System.currentTimeMillis();

            // 存储验证码（5 分钟有效）
            captchaStore.put(captchaId, captchaText + "_" + timestamp);
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
            String storedInfo = captchaStore.get(captchaId);
            if (storedInfo == null) return null;

            // 提取验证码文本
            String captchaText = storedInfo.split("_")[0];
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
        String storedInfo = captchaStore.get(captchaId);
        System.out.println("Captcha ID received: " + captchaId);  // 打印验证码 ID
        System.out.println("Captcha Text received: " + captchaText);  // 打印验证码文本

        if (storedInfo == null) {
            System.out.println("Captcha ID not found in store");
            return false;  // 如果没有找到验证码 ID
        }

        String storedCaptcha = storedInfo.split("_")[0];  // 提取存储的验证码文本
        System.out.println("Stored Captcha: " + storedCaptcha);  // 打印存储的验证码文本

        boolean isValid = storedCaptcha.equalsIgnoreCase(captchaText);

        // 如果校验成功，删除验证码
        if (isValid) {
            captchaStore.remove(captchaId);
            System.out.println("Captcha is valid, removing from store");
        } else {
            System.out.println("Captcha is invalid");
        }

        return isValid;
    }

    /**
     * 清理过期验证码
     */
    private void cleanExpiredCaptchas() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> {
            String[] parts = entry.getValue().split("_");
            long createTime = Long.parseLong(parts[1]);
            return now - createTime > 5 * 60 * 1000; // 5 分钟过期
        });
    }
}


