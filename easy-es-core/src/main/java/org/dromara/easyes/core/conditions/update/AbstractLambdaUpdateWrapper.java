package org.dromara.easyes.core.conditions.update;

import org.dromara.easyes.common.params.SFunction;
import org.dromara.easyes.core.kernel.AbstractWrapper;

/**
 * 抽象Lambda表达式父类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public abstract class AbstractLambdaUpdateWrapper<T, Children extends AbstractLambdaUpdateWrapper<T, Children>>
        extends AbstractWrapper<T, SFunction<T, ?>, Children> {
}
