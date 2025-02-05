package org.dromara.easyes.sample;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SpringBootApplication
@EsMapperScan("org.dromara.easyes.sample.mapper")
public class EasyEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyEsApplication.class, args);
    }
}
