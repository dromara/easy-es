package cn.easyes.core.conditions.select;

import cn.easyes.common.enums.EsQueryTypeEnum;
import cn.easyes.core.biz.AggregationParam;
import cn.easyes.core.biz.BaseSortParam;
import cn.easyes.core.biz.Param;

import java.util.LinkedList;
import java.util.List;

/**
 * 查询Lambda表达式
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("serial")
public class LambdaEsQueryWrapper<T> extends AbstractLambdaQueryWrapper<T, LambdaEsQueryWrapper<T>> {

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public LambdaEsQueryWrapper() {
        this(null);
    }

    public LambdaEsQueryWrapper(Class<T> entityClass) {
        super.initNeed();
        super.setEntityClass(entityClass);
        include = new String[]{};
        exclude = new String[]{};
    }

    LambdaEsQueryWrapper(T entity, int level, String parentId, EsQueryTypeEnum pervQueryType, LinkedList<Param> paramList,
                         LinkedList<String> parentIdQueue, LinkedList<EsQueryTypeEnum> prevQueryTypeQueue,
                         List<BaseSortParam> baseSortParams, List<AggregationParam> aggregationParamList) {
        super.setEntity(entity);
        this.level = level;
        this.parentId = parentId;
        this.prevQueryType = pervQueryType;
        this.paramQueue = paramList;
        this.parentIdQueue = parentIdQueue;
        this.prevQueryTypeQueue = prevQueryTypeQueue;
        this.baseSortParams = baseSortParams;
        this.aggregationParamList = aggregationParamList;
    }

    @Override
    protected LambdaEsQueryWrapper<T> instance() {
        return new LambdaEsQueryWrapper<>(entity, level, parentId, prevQueryType, paramQueue, parentIdQueue, prevQueryTypeQueue, baseSortParams, aggregationParamList);
    }

}
