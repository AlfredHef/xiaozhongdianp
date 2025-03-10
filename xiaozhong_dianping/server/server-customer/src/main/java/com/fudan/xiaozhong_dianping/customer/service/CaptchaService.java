package com.fudan.xiaozhong_dianping.customer.service;
public interface CaptchaService {
    boolean validateCaptcha(String userInputCaptcha);
    void saveCaptcha(String captcha); // 新增方法用于存储验证码
}
