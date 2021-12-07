package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.conditions.interfaces.SFunction;

/**
 * 抽象Lambda表达式父类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public abstract class AbstractLambdaQueryWrapper<T, Children extends AbstractLambdaQueryWrapper<T, Children>>
        extends AbstractWrapper<T, SFunction<T, ?>, Children> {

    protected T entity;

    @Override
    public Children setEntity(T entity) {
        this.entity = entity;
        return typedThis;
    }
}
