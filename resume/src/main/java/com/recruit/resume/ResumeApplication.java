package com.recruit.resume;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** 简历服务启动类 */
@SpringBootApplication
@ComponentScan("com.recruit")
@MapperScan("com.recruit.resume.mapper")
@EnableFeignClients(basePackages = "com.recruit.api")
public class ResumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeApplication.class, args);
    }
}
