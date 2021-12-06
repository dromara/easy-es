package com.xpc.easyes.core.conditions.interfaces;

import java.io.Serializable;

/**
 * 函数式接口
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 用于获取字段名称
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@FunctionalInterface
public interface SFunction<T, R> extends Serializable {
    /**
     * @param t
     * @return
     */
    R apply(T t);
}
