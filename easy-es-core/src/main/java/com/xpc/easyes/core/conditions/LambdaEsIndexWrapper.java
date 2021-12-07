package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.conditions.interfaces.Index;
import com.xpc.easyes.core.conditions.interfaces.SFunction;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.core.params.EsIndexParam;
import com.xpc.easyes.core.toolkit.FieldUtils;
import com.xpc.easyes.core.toolkit.StringUtils;
import org.elasticsearch.action.search.SearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 索引Lambda表达式
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("serial")
public class LambdaEsIndexWrapper<T> extends Wrapper<T> implements Index<LambdaEsIndexWrapper<T>, SFunction<T, ?>> {

    protected String indexName;
    protected String aliasName;
    protected Integer shardsNum;
    protected Integer replicasNum;

    List<EsIndexParam> esIndexParamList;
    private final T entity;
    protected final LambdaEsIndexWrapper<T> typedThis = this;

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public LambdaEsIndexWrapper() {
        this(null);
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     * @param entity 实体
     */
    public LambdaEsIndexWrapper(T entity) {
        this.entity = entity;
        esIndexParamList = new ArrayList<>();
    }

    @Override
    protected SearchRequest getSearchRequest() {
        return null;
    }

    @Override
    public LambdaEsIndexWrapper<T> indexName(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            throw new RuntimeException("indexName can not be empty");
        }
        this.indexName = indexName;
        return typedThis;
    }


    @Override
    public LambdaEsIndexWrapper<T> settings(Integer shards, Integer replicas) {
        if (Objects.nonNull(shards)) {
            this.shardsNum = shards;
        }
        if (Objects.nonNull(replicas)) {
            this.replicasNum = replicas;
        }
        return typedThis;
    }

    @Override
    public LambdaEsIndexWrapper<T> mapping(SFunction<T, ?> column, FieldType fieldType) {
        String fieldName = FieldUtils.getFieldName(column);
        EsIndexParam esIndexParam = new EsIndexParam();
        esIndexParam.setFieldName(fieldName);
        esIndexParam.setFieldType(fieldType.getType());
        esIndexParamList.add(esIndexParam);
        return typedThis;
    }

    @Override
    public LambdaEsIndexWrapper<T> createAlias(String aliasName) {
        if (StringUtils.isEmpty(indexName)) {
            throw new RuntimeException("indexName can not be empty");
        }
        if (StringUtils.isEmpty(aliasName)) {
            throw new RuntimeException("aliasName can not be empty");
        }
        this.aliasName = aliasName;
        return typedThis;
    }
}
