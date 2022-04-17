package com.xpc.easyes.core.toolkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.logging.Logger;

/**
 * 日志工具类,用于需要打印日志的地方,可避免引入其它日志框架依赖
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogUtils {
    private final static Logger log = Logger.getAnonymousLogger();

    /**
     * 打印日志
     *
     * @param params 参数
     */
    public static void info(String... params) {
        log.info(String.join(",", params));
    }
}
