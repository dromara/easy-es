package com.xpc.easyes.core.conditions.interfaces;

import java.io.Serializable;

/**
 * 函数式接口
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@FunctionalInterface
public interface SFunction<T, R> extends Serializable {
    R apply(T t);
}
