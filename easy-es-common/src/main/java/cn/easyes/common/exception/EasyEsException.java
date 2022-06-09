package cn.easyes.common.exception;

/**
 * EasyEs异常类
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
