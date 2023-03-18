package cn.easyes.core.conditions.index;


import cn.easyes.common.params.SFunction;
import cn.easyes.core.core.AbstractChainWrapper;
import cn.easyes.core.core.BaseEsMapper;

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
