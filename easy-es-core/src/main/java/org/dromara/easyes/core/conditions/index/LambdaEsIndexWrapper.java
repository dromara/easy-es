package org.dromara.easyes.core.conditions.index;


import org.dromara.easyes.core.biz.EsIndexParam;

import java.util.ArrayList;
import java.util.List;

/**
 * 索引Lambda表达式
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("serial")
public class LambdaEsIndexWrapper<T> extends AbstractLambdaIndexWrapper<T, LambdaEsIndexWrapper<T>> {
    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public LambdaEsIndexWrapper() {
        this(null);
    }

    public LambdaEsIndexWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
        esIndexParamList = new ArrayList<>();
    }

    LambdaEsIndexWrapper(T entity, List<EsIndexParam> indexParamList) {
        super.setEntity(entity);
        this.esIndexParamList = indexParamList;
    }

    @Override
    protected LambdaEsIndexWrapper<T> instance() {
        return new LambdaEsIndexWrapper<>(entity, esIndexParamList);
    }

}
