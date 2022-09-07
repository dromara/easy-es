package cn.easyes.core.conditions;

import cn.easyes.common.enums.FieldType;
import cn.easyes.common.params.SFunction;
import cn.easyes.common.utils.StringUtils;
import cn.easyes.core.biz.EsIndexParam;
import cn.easyes.core.conditions.interfaces.Index;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.settings.Settings;

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
     * 用户手动指定的索引mapping信息,优先级最高
     */
    protected Map<String, Object> mapping;
    /**
     * 用户手动指定的索引settings,优先级最高
     */
    protected Settings settings;
    /**
     * 索引相关参数列表
     */
    List<EsIndexParam> esIndexParamList;
    /**
     * 对应实体类
     */
    private final Class<T> entityClass;


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

    public LambdaEsIndexWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
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
    public LambdaEsIndexWrapper<T> settings(Settings settings) {
        this.settings = settings;
        return typedThis;
    }

    @Override
    public LambdaEsIndexWrapper<T> mapping(Map<String, Object> mapping) {
        this.mapping = mapping;
        return typedThis;
    }

    @Override
    public LambdaEsIndexWrapper<T> mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat, Boolean fieldData, Float boost) {
        addEsIndexParam(column, fieldType, analyzer, analyzer, dateFormat, fieldData, boost);
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

    @Override
    public LambdaEsIndexWrapper<T> join(String column, String parentName, String childName) {
        EsIndexParam esIndexParam = new EsIndexParam();
        esIndexParam.setFieldName(column);
        esIndexParam.setParentName(parentName);
        esIndexParam.setChildName(childName);
        esIndexParam.setFieldType(FieldType.JOIN.getType());
        esIndexParamList.add(esIndexParam);
        return typedThis;
    }

    private void addEsIndexParam(String fieldName, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat, Boolean fieldData, Float boost) {
        EsIndexParam esIndexParam = new EsIndexParam();
        esIndexParam.setFieldName(fieldName);
        esIndexParam.setFieldType(fieldType.getType());
        esIndexParam.setAnalyzer(analyzer);
        esIndexParam.setSearchAnalyzer(searchAnalyzer);
        esIndexParam.setDateFormat(dateFormat);
        esIndexParam.setFieldData(fieldData);
        esIndexParam.setBoost(boost);
        esIndexParamList.add(esIndexParam);
    }
}
