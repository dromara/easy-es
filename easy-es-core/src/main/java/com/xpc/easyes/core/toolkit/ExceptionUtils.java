package com.xpc.easyes.core.toolkit;


import com.xpc.easyes.core.exception.EasyEsException;

/**
 * 异常辅助工具类
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 便捷抛出异常时需要
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * 返回一个新的异常，统一构建，方便统一处理
     *
     * @param msg 消息
     * @param t   异常信息
     * @return 返回异常
     */
    public static EasyEsException eee(String msg, Throwable t, Object... params) {
        return new EasyEsException(String.format(msg, params), t);
    }

    /**
     * 重载的方法
     *
     * @param msg 消息
     * @return 返回异常
     */
    public static EasyEsException eee(String msg, Object... params) {
        return new EasyEsException(String.format(msg, params));
    }

    /**
     * 重载的方法
     *
     * @param t 异常
     * @return 返回异常
     */
    public static EasyEsException eee(Throwable t) {
        return new EasyEsException(t);
    }

}
