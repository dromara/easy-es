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
    /**
     * 日志打印工具名称
     */
    private final static String LOGGER_NAME = "easy-es";
    /**
     * logger
     */
    private final static Logger log = Logger.getLogger(LOGGER_NAME);

    /**
     * 打印info级别日志
     *
     * @param params 参数
     */
    public static void info(String... params) {
        log.info(String.join(",", params));
    }

    /**
     * 打印warn级别日志
     *
     * @param params 参数
     */
    public static void warn(String... params) {
        log.warning(String.join(",", params));
    }

    /**
     * 打印server(error)级别日志
     *
     * @param params 参数
     */
    public static void error(String... params) {
        log.severe(String.join(",", params));
    }


}
