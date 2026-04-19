package com.example.meridian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 系统启动类 */
@SpringBootApplication
@MapperScan("com.example.meridian.mapper")
@EnableScheduling
public class MeridianHealthApplication {
    public static void main(String[] args) { SpringApplication.run(MeridianHealthApplication.class, args); }
}
