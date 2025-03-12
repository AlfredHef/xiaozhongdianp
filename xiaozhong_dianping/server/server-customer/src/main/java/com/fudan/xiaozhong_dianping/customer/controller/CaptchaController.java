//package com.fudan.xiaozhong_dianping.customer.controller;
//
//import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
//import org.springframework.web.bind.annotation.*;
//
//import javax.imageio.ImageIO;
//import javax.servlet.http.HttpServletResponse;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/captcha")
//public class CaptchaController {
//    private final CaptchaService captchaService;
//
//    public CaptchaController(CaptchaService captchaService) {
//        this.captchaService = captchaService;
//    }
//
//    /**
//     * 生成验证码 ID
//     */
//    @GetMapping("/generate")
//    public Map<String, String> generateCaptcha() {
//        String captchaId = captchaService.generateCaptcha();
//        Map<String, String> response = new HashMap<>();
//        response.put("captchaId", captchaId);
//        return response;
//    }
//
//    /**
//     * 获取验证码图片
//     */
//    @GetMapping("/image/{captchaId}")
//    public void getCaptchaImage(@PathVariable String captchaId, HttpServletResponse response) throws IOException {
//        BufferedImage captchaImage = captchaService.getCaptchaImage(captchaId);
//        if (captchaImage == null) {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//
//        response.setContentType("image/png");
//        ImageIO.write(captchaImage, "png", response.getOutputStream());
//    }
//
//    /**
//     * 校验验证码
//     */
//    @PostMapping("/validate")
//    public Map<String, Boolean> validateCaptcha(@RequestParam String captchaId, @RequestParam String userInput) {
//        boolean isValid = captchaService.validateCaptcha(captchaId, userInput);
//        Map<String, Boolean> response = new HashMap<>();
//        response.put("isValid", isValid);
//        return response;
//    }
//}

package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.xiaozhong_dianping.customer.service.CaptchaService;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
    @GetMapping
    public Map<String, Object> getCaptcha(HttpServletResponse response) {
        try {
            String captchaId = captchaService.generateCaptcha();
            BufferedImage captchaImage = captchaService.getCaptchaImage(captchaId);
            if (captchaImage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(captchaImage, "png", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();

            String encodedImage = Base64.getEncoder().encodeToString(imageInByte);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("captchaId", captchaId);
            responseMap.put("captchaImage", encodedImage);
            return responseMap;
        } catch (Exception e) {
            logger.error("Failed to get captcha", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
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