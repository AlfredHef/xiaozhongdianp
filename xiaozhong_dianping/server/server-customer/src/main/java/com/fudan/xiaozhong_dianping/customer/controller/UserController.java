package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.constant.JwtClaimsConstant;
import com.fudan.dto.ResponseDTO;
import com.fudan.dto.UserLoginDTO;
import com.fudan.entity.RegisterRequest;
import com.fudan.result.Result;
import com.fudan.vo.UserLoginVO;
import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;
import com.fudan.xiaozhong_dianping.customer.properties.JwtProperties;
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import com.fudan.entity.User;

import com.fudan.xiaozhong_dianping.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseDTO<String> register(@RequestBody RegisterRequest registerRequest) {
        // 调用 service 处理注册
        RegisterResult result = userService.register(registerRequest);

        switch (result) {
            case SUCCESS:
                return new ResponseDTO<>(200, "注册成功", null);
            case USERNAME_INVALID:
                return new ResponseDTO<>(400, "注册失败，用户名格式不符合要求", null);
            case USERNAME_EXISTS:
                return new ResponseDTO<>(400, "注册失败，用户名已存在", null);
            case PASSWORD_INVALID:
                return new ResponseDTO<>(400, "注册失败，密码强度不足", null);
            case CAPTCHA_INVALID:
                return new ResponseDTO<>(400, "注册失败，验证码错误", null);
            default:
                return new ResponseDTO<>(400, "注册失败，未知错误", null);
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录:{}", userLoginDTO);
        User user = userService.login(userLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO userLoginVO=UserLoginVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
}
