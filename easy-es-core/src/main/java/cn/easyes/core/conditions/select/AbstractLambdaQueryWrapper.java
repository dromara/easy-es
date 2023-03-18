package cn.easyes.core.conditions.select;

import cn.easyes.common.params.SFunction;
import cn.easyes.core.core.AbstractWrapper;

/**
 * 抽象Lambda表达式父类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public abstract class AbstractLambdaQueryWrapper<T, Children extends AbstractLambdaQueryWrapper<T, Children>>
        extends AbstractWrapper<T, SFunction<T, ?>, Children> {
}
