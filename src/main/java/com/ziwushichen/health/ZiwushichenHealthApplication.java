package com.ziwushichen.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统启动类。
 */
@SpringBootApplication
@EnableScheduling
public class ZiwushichenHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZiwushichenHealthApplication.class, args);
    }
}
