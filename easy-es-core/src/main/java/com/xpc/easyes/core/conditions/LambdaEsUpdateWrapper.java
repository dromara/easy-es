package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.conditions.interfaces.SFunction;
import com.xpc.easyes.core.conditions.interfaces.Update;
import com.xpc.easyes.core.params.BaseEsParam;
import com.xpc.easyes.core.params.EsUpdateParam;
import com.xpc.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.action.search.SearchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新Lambda表达式
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 更新Lambda表达式 封装更新相关参数
 * @Author: xpc
 * @Version: 1.0
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

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public LambdaEsUpdateWrapper(T entity) {
        super.initNeed();
        super.setEntity(entity);
        updateParamList = new ArrayList<>();
    }

    LambdaEsUpdateWrapper(T entity, List<BaseEsParam> baseEsParamList, List<EsUpdateParam> updateParamList) {
        super.setEntity(entity);
        this.baseEsParamList = baseEsParamList;
        this.updateParamList = updateParamList;
    }

    @Override
    public LambdaEsUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            EsUpdateParam esUpdateParam = new EsUpdateParam();
            esUpdateParam.setField(FieldUtils.getFieldName(column));
            esUpdateParam.setValue(val);
            updateParamList.add(esUpdateParam);
        }
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
