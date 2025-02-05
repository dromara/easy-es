package org.dromara.easyes.test;

import org.dromara.easyes.solon.register.EsMapperScan;
import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;

/**
 * 启动类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SolonMain
@EsMapperScan("org.dromara.easyes.test.mapper")
public class TestEasyEsApplication {
    public static void main(String[] args) {
        Solon.start(TestEasyEsApplication.class, args);
    }
}
