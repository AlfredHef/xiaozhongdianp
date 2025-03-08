package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.dto.UserLoginDTO;
import com.fudan.properties.JwtProperties;
import com.fudan.result.Result;
import com.fudan.vo.UserLoginVO;
import com.fudan.xiaozhong_dianping.customer.service.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags="用户相关接口")
@RestController
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/customer/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录");
    }
}
