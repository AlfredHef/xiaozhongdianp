package com.fudan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录 DTO
 */
@Data
public class UserLoginDTO {

    private String username;


    private String password;

    private String captchaId;
    private String captchaText; // 新增验证码字段

}