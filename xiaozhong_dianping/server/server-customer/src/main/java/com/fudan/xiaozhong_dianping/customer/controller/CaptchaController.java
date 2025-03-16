package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    private final CaptchaService captchaService;
    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 生成验证码并返回验证码图片和 captchaId
     */
    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> getCaptcha(HttpServletResponse response) {
        try {
            String captchaId = captchaService.generateCaptcha();
            BufferedImage captchaImage = captchaService.getCaptchaImage(captchaId);
            if (captchaImage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).build();
            }

            // 将验证码图片转换为 base64 编码格式，前端可以直接使用
            ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
            ImageIO.write(captchaImage, "png", imageOutputStream);
            String captchaImageBase64 = Base64.getEncoder().encodeToString(imageOutputStream.toByteArray());

            // 构造响应体，包含 captchaImage 和 captchaId
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("captchaImage", "data:image/png;base64," + captchaImageBase64);
            responseMap.put("captchaId", captchaId);

            return ResponseEntity.ok(responseMap);

        } catch (IOException e) {
            logger.error("Failed to generate captcha image", e);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Failed to get captcha", e);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 校验验证码
     */
    @PostMapping("/verify")
    public Map<String, Boolean> verifyCaptcha(@RequestParam String captchaId, @RequestParam String captchaText) {
        boolean isValid = captchaService.validateCaptcha(captchaId, captchaText);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        return response;
    }
}
