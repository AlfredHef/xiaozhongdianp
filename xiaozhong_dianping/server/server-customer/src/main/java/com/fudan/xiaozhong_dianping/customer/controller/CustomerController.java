package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.xiaozhong_dianping.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {
    @Autowired
    private CustomerService customerService;
}
