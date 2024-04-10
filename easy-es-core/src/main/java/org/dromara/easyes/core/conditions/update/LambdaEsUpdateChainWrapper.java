package org.dromara.easyes.core.conditions.update;


import org.dromara.easyes.common.params.SFunction;
import org.dromara.easyes.core.kernel.AbstractChainWrapper;
import org.dromara.easyes.core.kernel.BaseEsMapper;

/**
 * 链式更新条件构造器
 * <p>
 * Copyright © 2023 xpc1024 All Rights Reserved
 **/
@SuppressWarnings({"serial"})
public class LambdaEsUpdateChainWrapper<T> extends AbstractChainWrapper<T, SFunction<T, ?>, LambdaEsUpdateChainWrapper<T>, LambdaEsUpdateWrapper<T>>
        implements EsChainUpdate<T> {

    private final BaseEsMapper<T> baseEsMapper;

    public LambdaEsUpdateChainWrapper(BaseEsMapper<T> baseEsMapper) {
        super();
        this.baseEsMapper = baseEsMapper;
        super.wrapperChildren = new LambdaEsUpdateWrapper<>();
    }


    @Override
    public BaseEsMapper<T> getBaseEsMapper() {
        return baseEsMapper;
    }


}
