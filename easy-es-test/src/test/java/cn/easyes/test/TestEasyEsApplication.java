package cn.easyes.test;

import cn.easyes.starter.register.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SpringBootApplication
@EsMapperScan("cn.easyes.test.mapper")
public class TestEasyEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestEasyEsApplication.class, args);
    }
}
