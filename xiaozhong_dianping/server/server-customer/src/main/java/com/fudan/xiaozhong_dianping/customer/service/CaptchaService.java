//package com.fudan.xiaozhong_dianping.customer.service;
//public interface CaptchaService {
//    boolean validateCaptcha(String userInputCaptcha);
//    void saveCaptcha(String captcha); // 新增方法用于存储验证码
//}

package com.fudan.xiaozhong_dianping.customer.service;

import java.awt.image.BufferedImage;

public interface CaptchaService {
    /**
     * 生成验证码并返回验证码 ID
     */
    String generateCaptcha();

    /**
     * 获取验证码图片
     */
    BufferedImage getCaptchaImage(String captchaId);

    /**
     * 校验用户输入的验证码是否正确
     */
    boolean validateCaptcha(String captchaId, String userInput);
}
