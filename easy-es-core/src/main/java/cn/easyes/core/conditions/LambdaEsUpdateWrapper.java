package cn.easyes.core.conditions;

import cn.easyes.common.params.SFunction;
import cn.easyes.core.biz.BaseEsParam;
import cn.easyes.core.biz.EsUpdateParam;
import cn.easyes.core.conditions.interfaces.Update;
import org.elasticsearch.action.search.SearchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新Lambda表达式
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("serial")
public class LambdaEsUpdateWrapper<T> extends AbstractLambdaUpdateWrapper<T, LambdaEsUpdateWrapper<T>>
        implements Update<LambdaEsUpdateWrapper<T>, SFunction<T, ?>> {

    List<EsUpdateParam> updateParamList;

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public LambdaEsUpdateWrapper() {
        this(null);
    }

    public LambdaEsUpdateWrapper(Class<T> entityClass) {
        super.initNeed();
        super.setEntityClass(entityClass);
        updateParamList = new ArrayList<>();
    }

    LambdaEsUpdateWrapper(T entity, List<BaseEsParam> baseEsParamList, List<EsUpdateParam> updateParamList) {
        super.setEntity(entity);
        this.baseEsParamList = baseEsParamList;
        this.updateParamList = updateParamList;
    }

    @Override
    public LambdaEsUpdateWrapper<T> set(boolean condition, String column, Object val) {
        if (condition) {
            EsUpdateParam esUpdateParam = new EsUpdateParam();
            esUpdateParam.setField(column);
            esUpdateParam.setValue(val);
            updateParamList.add(esUpdateParam);
        }
        return typedThis;
    }

    @Override
    public LambdaEsUpdateWrapper<T> index(boolean condition, String indexName) {
        this.indexName = indexName;
        return typedThis;
    }

    @Override
    protected LambdaEsUpdateWrapper<T> instance() {
        return new LambdaEsUpdateWrapper<>(entity, baseEsParamList, updateParamList);
    }

    @Override
    protected SearchRequest getSearchRequest() {
        return null;
    }
}
