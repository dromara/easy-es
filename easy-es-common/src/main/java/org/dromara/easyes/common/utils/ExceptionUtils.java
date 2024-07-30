package org.dromara.easyes.common.utils;


import org.dromara.easyes.common.exception.EasyEsException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * 异常辅助工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * 返回一个新的异常，统一构建，方便统一处理
     *
     * @param msg    消息
     * @param t      异常
     * @param params 参数
     * @return 自定义异常
     */
    public static EasyEsException eee(String msg, Throwable t, Object... params) {
        return new EasyEsException(String.format(msg, params), t);
    }

    /**
     * 重载
     *
     * @param msg 消息
     * @return 自定义异常
     */
    public static EasyEsException eee(String msg) {
        return new EasyEsException(msg);
    }

    /**
     * 重载
     *
     * @param msg    消息
     * @param params 参数
     * @return 自定义异常
     */
    public static EasyEsException eee(String msg, Object... params) {
        return new EasyEsException(String.format(msg, params));
    }

    /**
     * 重载
     *
     * @param t 异常
     * @return 自定义异常
     */
    public static EasyEsException eee(Throwable t) {
        return new EasyEsException(t);
    }

    /**
     * 异常包装
     *
     * @param wrapped 异常
     * @return 异常
     */
    public static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

}
