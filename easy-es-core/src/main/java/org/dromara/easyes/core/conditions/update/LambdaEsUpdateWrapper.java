package org.dromara.easyes.core.conditions.update;

import org.dromara.easyes.common.enums.EsQueryTypeEnum;
import org.dromara.easyes.common.params.SFunction;
import org.dromara.easyes.core.biz.EsUpdateParam;
import org.dromara.easyes.core.biz.Param;
import org.dromara.easyes.core.conditions.function.Update;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 更新Lambda表达式
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("serial")
public class LambdaEsUpdateWrapper<T> extends AbstractLambdaUpdateWrapper<T, LambdaEsUpdateWrapper<T>>
        implements Update<LambdaEsUpdateWrapper<T>, SFunction<T, ?>> {
    /**
     * 不建议直接 new 该实例，使用 EsWrappers.lambdaQuery(entity)
     */
    public LambdaEsUpdateWrapper() {
        this(null);
    }

    public LambdaEsUpdateWrapper(Class<T> entityClass) {
        super.initNeed();
        super.setEntityClass(entityClass);
        updateParamList = new ArrayList<>();
        paramQueue = new LinkedList<>();
    }

    LambdaEsUpdateWrapper(T entity, int level, String parentId, EsQueryTypeEnum pervQueryType, LinkedList<Param> paramQueue,
                          Stack<String> parentIdStack, LinkedList<EsQueryTypeEnum> prevQueryTypeQueue, List<EsUpdateParam> updateParamList) {
        super.setEntity(entity);
        this.level = level;
        this.parentId = parentId;
        this.prevQueryType = pervQueryType;
        this.paramQueue = paramQueue;
        this.parentIdStack = parentIdStack;
        this.prevQueryTypeQueue = prevQueryTypeQueue;
        this.updateParamList = updateParamList;
    }

    @Override
    protected LambdaEsUpdateWrapper<T> instance() {
        return new LambdaEsUpdateWrapper<>(entity, level, parentId, prevQueryType, paramQueue, parentIdStack, prevQueryTypeQueue, updateParamList);
    }

}
