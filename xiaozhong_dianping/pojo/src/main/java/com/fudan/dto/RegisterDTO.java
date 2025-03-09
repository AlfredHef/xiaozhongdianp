package com.fudan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册 DTO
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含字母、数字和下划线，长度为4-20")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能小于6位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$",
            message = "密码必须包含字母和数字")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captcha;
}