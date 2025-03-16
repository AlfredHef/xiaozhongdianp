package com.fudan.entity;

public class RegisterRequest {
    private User user;  // 用户信息，包括 username 和 password
    private String captchaId;  // 验证码 ID
    private String captchaText;  // 用户输入的验证码文本

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaText() {
        return captchaText;
    }

    public void setCaptchaText(String captchaText) {
        this.captchaText = captchaText;
    }
}
