package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.conditions.interfaces.Index;
import com.xpc.easyes.core.conditions.interfaces.SFunction;
import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.core.params.EsIndexParam;
import com.xpc.easyes.core.toolkit.FieldUtils;
import com.xpc.easyes.core.toolkit.StringUtils;
import org.elasticsearch.action.search.SearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 索引Lambda表达式
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("serial")
public class LambdaEsIndexWrapper<T> extends Wrapper<T> implements Index<LambdaEsIndexWrapper<T>, SFunction<T, ?>> {
    /**
     * 索引名称
     */
    protected String indexName;
    /**
     * 别名
     */
    protected String aliasName;
    /**
     * 分片数
     */
    protected Integer shardsNum;
    /**
     * 副本数
     */
    protected Integer replicasNum;
    /**
     * 用户手动指定的mapping信息,优先级最高
     */
    protected Map<String, Object> mapping;
    /**
     * 索引相关参数列表
     */
    List<EsIndexParam> esIndexParamList;
    /**
     * 对应实体
     */
    private final T entity;
    /**
     * 此包装类本身
     */
    protected final LambdaEsIndexWrapper<T> typedThis = this;

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public LambdaEsIndexWrapper() {
        this(null);
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     *
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
    public LambdaEsIndexWrapper<T> mapping(Map<String, Object> mapping) {
        this.mapping = mapping;
        return typedThis;
    }

    @Override
    public LambdaEsIndexWrapper<T> mapping(SFunction<T, ?> column, FieldType fieldType, Analyzer analyzer, Analyzer searchAnalyzer) {
        String fieldName = FieldUtils.getFieldName(column);
        addEsIndexParam(fieldName, fieldType, analyzer, analyzer);
        return typedThis;
    }

    @Override
    public LambdaEsIndexWrapper<T> mapping(String column, FieldType fieldType, Analyzer analyzer, Analyzer searchAnalyzer) {
        addEsIndexParam(column, fieldType, analyzer, analyzer);
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

    private void addEsIndexParam(String fieldName, FieldType fieldType, Analyzer analyzer, Analyzer searchAnalyzer) {
        EsIndexParam esIndexParam = new EsIndexParam();
        esIndexParam.setFieldName(fieldName);
        esIndexParam.setFieldType(fieldType.getType());
        esIndexParam.setAnalyzer(analyzer);
        esIndexParam.setSearchAnalyzer(searchAnalyzer);
        esIndexParamList.add(esIndexParam);
    }
}
