package com.xpc.easyes.core.exception;

/**
 * EasyEs异常类
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 异常类
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class EasyEsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EasyEsException(String message) {
        super(message);
    }

    public EasyEsException(Throwable throwable) {
        super(throwable);
    }

    public EasyEsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
