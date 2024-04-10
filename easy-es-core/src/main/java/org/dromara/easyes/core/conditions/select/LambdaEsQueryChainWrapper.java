package org.dromara.easyes.core.conditions.select;

import org.dromara.easyes.common.params.SFunction;
import org.dromara.easyes.core.kernel.AbstractChainWrapper;
import org.dromara.easyes.core.kernel.BaseEsMapper;
import org.dromara.easyes.core.conditions.function.Query;

/**
 * 链式调用Lambda表达式
 * <p>
 * Copyright © 2023 xpc1024 All Rights Reserved
 **/
@SuppressWarnings({"serial"})
public class LambdaEsQueryChainWrapper<T> extends AbstractChainWrapper<T, SFunction<T, ?>, LambdaEsQueryChainWrapper<T>, LambdaEsQueryWrapper<T>>
        implements EsChainQuery<T>, Query<LambdaEsQueryChainWrapper<T>, T, SFunction<T, ?>> {

    private final BaseEsMapper<T> baseEsMapper;

    public LambdaEsQueryChainWrapper(BaseEsMapper<T> baseEsMapper) {
        super();
        this.baseEsMapper = baseEsMapper;
        super.wrapperChildren = new LambdaEsQueryWrapper<>();
    }

    @Override
    public BaseEsMapper<T> getBaseEsMapper() {
        return baseEsMapper;
    }

}
