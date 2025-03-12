package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
    public void getCaptcha(HttpServletResponse response) {
        try {
            String captchaId = captchaService.generateCaptcha();
            BufferedImage captchaImage = captchaService.getCaptchaImage(captchaId);
            if (captchaImage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 设置响应的 Content-Type 为 image/png
            response.setContentType("image/png");
            // 将图片写入响应的输出流
            ImageIO.write(captchaImage, "png", response.getOutputStream());
            // 设置响应头，将 captchaId 作为自定义头返回
            response.setHeader("Captcha-Id", captchaId);
            logger.info("captchaId: " + captchaId);
        } catch (IOException e) {
            logger.error("Failed to generate captcha image", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Failed to get captcha", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 校验验证码
     */
    @PostMapping("/verify")
    public java.util.Map<String, Boolean> verifyCaptcha(@RequestParam String captchaId, @RequestParam String captchaText) {
        boolean isValid = captchaService.validateCaptcha(captchaId, captchaText);
        java.util.Map<String, Boolean> response = new java.util.HashMap<>();
        response.put("isValid", isValid);
        return response;
    }
}