package org.dromara.easyes.test;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SpringBootApplication
@EsMapperScan({"org.dromara.easyes.test.mapper", "org.dromara.easyes.test.mapper2", "org.dromara.easyes.test.mapper3"})
public class TestEasyEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestEasyEsApplication.class, args);
    }
}
