package com.recruit.position;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 职位服务启动类
 */
@SpringBootApplication
@ComponentScan("com.recruit")
@MapperScan("com.recruit.position.mapper")
public class PositionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PositionApplication.class, args);
    }
}
