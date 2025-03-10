package com.fudan.xiaozhong_dianping.customer.service;
public interface CaptchaService {
    boolean validateCaptcha(String userInputCaptcha);
}