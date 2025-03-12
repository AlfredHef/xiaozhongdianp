package com.fudan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 允许前端访问的域名（需替换为前端项目地址，如前端启动端口是 8080）
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5175") // 允许前端地址访问
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                        .allowCredentials(true) // 允许携带 Cookie
                        .allowedHeaders("*");
            }
        };
    }
}