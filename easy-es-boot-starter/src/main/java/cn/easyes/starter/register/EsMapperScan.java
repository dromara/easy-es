package cn.easyes.starter.register;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 全局Mapper扫描注解
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(MapperScannerRegister.class)
public @interface EsMapperScan {
    String value();
}
