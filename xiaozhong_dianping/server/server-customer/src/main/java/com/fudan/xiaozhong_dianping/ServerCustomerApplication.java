package com.fudan.xiaozhong_dianping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class ServerCustomerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerCustomerApplication.class, args);
        log.info("customer server start");
    }
}
