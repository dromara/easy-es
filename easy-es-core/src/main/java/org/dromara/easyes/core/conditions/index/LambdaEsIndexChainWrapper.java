package org.dromara.easyes.core.conditions.index;


import org.dromara.easyes.common.params.SFunction;
import org.dromara.easyes.core.kernel.AbstractChainWrapper;
import org.dromara.easyes.core.kernel.BaseEsMapper;

/**
 * 链式索引条件构造器
 * <p>
 * Copyright © 2023 xpc1024 All Rights Reserved
 **/
@SuppressWarnings({"serial"})
public class LambdaEsIndexChainWrapper<T> extends AbstractChainWrapper<T, SFunction<T, ?>, LambdaEsIndexChainWrapper<T>, LambdaEsIndexWrapper<T>>
        implements EsChainIndex<T> {

    private final BaseEsMapper<T> baseEsMapper;

    public LambdaEsIndexChainWrapper(BaseEsMapper<T> baseEsMapper) {
        super();
        this.baseEsMapper = baseEsMapper;
        super.wrapperChildren = new LambdaEsIndexWrapper<>();
    }

    @Override
    public BaseEsMapper<T> getBaseEsMapper() {
        return baseEsMapper;
    }

}
