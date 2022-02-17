package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.common.EntityFieldInfo;
import com.xpc.easyes.core.conditions.interfaces.Query;
import com.xpc.easyes.core.conditions.interfaces.SFunction;
import com.xpc.easyes.core.params.AggregationParam;
import com.xpc.easyes.core.params.BaseEsParam;
import com.xpc.easyes.core.params.HighLightParam;
import com.xpc.easyes.core.params.SortParam;
import com.xpc.easyes.core.toolkit.ArrayUtils;
import com.xpc.easyes.core.toolkit.EntityInfoHelper;
import com.xpc.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.action.search.SearchRequest;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 查询Lambda表达式
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("serial")
public class LambdaEsQueryWrapper<T> extends AbstractLambdaQueryWrapper<T, LambdaEsQueryWrapper<T>>
        implements Query<LambdaEsQueryWrapper<T>, T, SFunction<T, ?>> {
    /**
     * 查询字段
     */
    protected String[] include;
    /**
     * 不查字段
     */
    protected String[] exclude;
    /**
     * 从第多少条开始查询
     */
    protected Integer from;
    /**
     * 查询多少条记录
     */
    protected Integer size;

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public LambdaEsQueryWrapper() {
        this(null);
        include = new String[]{};
        exclude = new String[]{};
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     *
     * @param entity 实体
     */
    public LambdaEsQueryWrapper(T entity) {
        super.initNeed();
        super.setEntity(entity);
        include = new String[]{};
        exclude = new String[]{};
    }

    LambdaEsQueryWrapper(T entity, List<BaseEsParam> baseEsParamList, List<HighLightParam> highLightParamList,
                         List<SortParam> sortParamList, List<AggregationParam> aggregationParamList) {
        super.setEntity(entity);
        include = new String[]{};
        exclude = new String[]{};
        this.baseEsParamList = baseEsParamList;
        this.highLightParamList = highLightParamList;
        this.sortParamList = sortParamList;
        this.aggregationParamList = aggregationParamList;
    }

    @Override
    protected LambdaEsQueryWrapper<T> instance() {
        return new LambdaEsQueryWrapper<>(entity, baseEsParamList, highLightParamList, sortParamList, aggregationParamList);
    }

    @Override
    public LambdaEsQueryWrapper<T> select(SFunction<T, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            List<String> list = Arrays.stream(columns)
                    .map(FieldUtils::getFieldName)
                    .collect(Collectors.toList());
            include = list.toArray(include);
        }
        return typedThis;
    }

    @Override
    public LambdaEsQueryWrapper<T> select(Predicate<EntityFieldInfo> predicate) {
        return select(entityClass, predicate);
    }

    @Override
    public LambdaEsQueryWrapper<T> select(Class<T> entityClass, Predicate<EntityFieldInfo> predicate) {
        this.entityClass = entityClass;
        List<String> list = EntityInfoHelper.getEntityInfo(getCheckEntityClass()).chooseSelect(predicate);
        include = list.toArray(include);
        return typedThis;
    }

    @Override
    public LambdaEsQueryWrapper<T> notSelect(SFunction<T, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            List<String> list = Arrays.stream(columns)
                    .map(FieldUtils::getFieldName)
                    .collect(Collectors.toList());
            exclude = list.toArray(exclude);
        }
        return typedThis;
    }

    @Override
    public LambdaEsQueryWrapper<T> from(Integer from) {
        this.from = from;
        return typedThis;
    }

    @Override
    public LambdaEsQueryWrapper<T> size(Integer size) {
        this.size = size;
        return typedThis;
    }

    @Override
    public LambdaEsQueryWrapper<T> limit(Integer m) {
        this.size = m;
        return typedThis;
    }

    @Override
    public LambdaEsQueryWrapper<T> limit(Integer m, Integer n) {
        this.from = m;
        this.size = n;
        return typedThis;
    }

    @Override
    protected SearchRequest getSearchRequest() {
        // TODO 待优化
        return null;
    }

}
