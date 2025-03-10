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
    // 统一用户名规则为 3-20 位，匹配服务层逻辑
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名只能包含字母、数字和下划线，长度为 3-20")
    private String username;

    // 强化密码规则：至少包含一个大写字母、一个小写字母、一个数字，长度 8-20 位
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度需在 8-20 位之间")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$",
            message = "密码必须包含大写字母、小写字母和数字，长度 8-20 位"
    )
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captcha;
}