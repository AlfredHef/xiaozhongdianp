package com.fudan.xiaozhong_dianping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ServerCustomerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerCustomerApplication.class, args);
        log.info("customer server start");
    }
}
