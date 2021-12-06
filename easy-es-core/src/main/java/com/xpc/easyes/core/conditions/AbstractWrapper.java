package com.xpc.easyes.core.conditions;

import com.xpc.easyes.core.conditions.interfaces.Compare;
import com.xpc.easyes.core.conditions.interfaces.Func;
import com.xpc.easyes.core.conditions.interfaces.Join;
import com.xpc.easyes.core.conditions.interfaces.Nested;
import com.xpc.easyes.core.enums.AggregationTypeEnum;
import com.xpc.easyes.core.enums.BaseEsParamTypeEnum;
import com.xpc.easyes.core.enums.EsAttachTypeEnum;
import com.xpc.easyes.core.enums.EsQueryTypeEnum;
import com.xpc.easyes.core.params.AggregationParam;
import com.xpc.easyes.core.params.BaseEsParam;
import com.xpc.easyes.core.params.HighLightParam;
import com.xpc.easyes.core.params.SortParam;
import com.xpc.easyes.core.toolkit.ArrayUtils;
import com.xpc.easyes.core.toolkit.Assert;
import com.xpc.easyes.core.toolkit.CollectionUtils;
import com.xpc.easyes.core.toolkit.FieldUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xpc.easyes.core.enums.BaseEsParamTypeEnum.*;
import static com.xpc.easyes.core.enums.EsAttachTypeEnum.MUST;
import static com.xpc.easyes.core.enums.EsAttachTypeEnum.MUST_NOT;
import static com.xpc.easyes.core.enums.EsQueryTypeEnum.*;

/**
 * 抽象Lambda表达式父类
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 抽象Lambda表达式父类, 用于完成参数的初步封装
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public abstract class AbstractWrapper<T, R, Children extends AbstractWrapper<T, R, Children>> extends Wrapper<T>
        implements Compare<Children, R>, Nested<Children, Children>, Join<Children>, Func<Children, R> {

    protected final Children typedThis = (Children) this;

    /**
     * 基础查询参数列表
     */
    protected List<BaseEsParam> baseEsParamList;
    /**
     * 高亮查询参数列表
     */
    protected List<HighLightParam> highLightParamList;
    /**
     * 排序查询参数列表
     */
    protected List<SortParam> sortParamList;
    /**
     * 聚合查询参数列表
     */
    protected List<AggregationParam> aggregationParamList;

    /**
     * 实体对象
     */
    protected T entity;
    /**
     * 实体类型
     */
    protected Class<T> entityClass;

    public Children setEntity(T entity) {
        this.entity = entity;
        this.initEntityClass();
        return typedThis;
    }

    protected void initEntityClass() {
        if (this.entityClass == null && this.entity != null) {
            this.entityClass = (Class<T>) entity.getClass();
        }
    }

    protected Class<T> getCheckEntityClass() {
        Assert.notNull(entityClass, "entityClass must not null,please set entity before use this method!");
        return entityClass;
    }

    /**
     * 必要的初始化
     */
    protected final void initNeed() {
        baseEsParamList = new ArrayList<>();
        highLightParamList = new ArrayList<>();
        sortParamList = new ArrayList<>();
        aggregationParamList = new ArrayList<>();
    }

    @Override
    public Children eq(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, TERM_QUERY, MUST, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children ne(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, TERM_QUERY, MUST_NOT, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children and(boolean condition, Function<Children, Children> func) {
        return doIt(condition, func, AND_LEFT_BRACKET, AND_RIGHT_BRACKET);
    }

    @Override
    public Children or(boolean condition, Function<Children, Children> func) {
        return doIt(condition, func, OR_LEFT_BRACKET, OR_RIGHT_BRACKET);
    }

    @Override
    public Children match(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, MATCH_QUERY, MUST, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children notMatch(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, MATCH_QUERY, MUST_NOT, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children gt(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.GT, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children ge(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.GE, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children lt(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.LT, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children le(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, RANGE_QUERY, EsAttachTypeEnum.LE, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children between(boolean condition, R column, Object val1, Object val2, Float boost) {
        return doIt(condition, EsAttachTypeEnum.BETWEEN, FieldUtils.getFieldName(column), val1, val2, boost);
    }

    @Override
    public Children notBetween(boolean condition, R column, Object val1, Object val2, Float boost) {
        return doIt(condition, EsAttachTypeEnum.NOT_BETWEEN, FieldUtils.getFieldName(column), val1, val2, boost);
    }

    @Override
    public Children or(boolean condition) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            baseEsParam.setType(BaseEsParamTypeEnum.OR_ALL.getType());
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    @Override
    public Children like(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, MUST, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children notLike(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, MUST_NOT, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children likeLeft(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, EsAttachTypeEnum.LIKE_LEFT, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children likeRight(boolean condition, R column, Object val, Float boost) {
        return doIt(condition, WILDCARD_QUERY, EsAttachTypeEnum.LIKE_RIGHT, FieldUtils.getFieldName(column), val, boost);
    }

    @Override
    public Children highLight(boolean condition, String preTag, String postTag, R column) {
        if (condition) {
            String fieldName = FieldUtils.getFieldName(column);
            List<String> fields = new ArrayList<>();
            fields.add(fieldName);
            highLightParamList.add(new HighLightParam(preTag, postTag, fields));
        }
        return typedThis;
    }

    @Override
    public Children highLight(boolean condition, String preTag, String postTag, R... columns) {
        if (condition) {
            List<String> fields = Arrays.stream(columns).map(FieldUtils::getFieldName).collect(Collectors.toList());
            highLightParamList.add(new HighLightParam(preTag, postTag, fields));
        }
        return typedThis;
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, R... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }

        if (condition) {
            List<String> fields = Arrays.stream(columns).map(FieldUtils::getFieldName).collect(Collectors.toList());
            sortParamList.add(new SortParam(isAsc, fields));
        }
        return typedThis;
    }

    @Override
    public Children in(boolean condition, R column, Collection<?> coll, Float boost) {
        if (CollectionUtils.isEmpty(coll)) {
            return typedThis;
        }
        return doIt(condition, EsAttachTypeEnum.IN, FieldUtils.getFieldName(column), new ArrayList<>(coll), boost);
    }

    @Override
    public Children notIn(boolean condition, R column, Collection<?> coll, Float boost) {
        if (CollectionUtils.isEmpty(coll)) {
            return typedThis;
        }
        return doIt(condition, EsAttachTypeEnum.NOT_IN, FieldUtils.getFieldName(column), new ArrayList<>(coll), boost);
    }

    @Override
    public Children isNull(boolean condition, R column, Float boost) {
        return doIt(condition, EsAttachTypeEnum.NOT_EXISTS, FieldUtils.getFieldName(column), boost);
    }

    @Override
    public Children isNotNull(boolean condition, R column, Float boost) {
        return doIt(condition, EsAttachTypeEnum.EXISTS, FieldUtils.getFieldName(column), boost);
    }

    @Override
    public Children groupBy(boolean condition, R... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Arrays.stream(columns).forEach(column -> {
            String returnName = FieldUtils.getFieldName(column);
            doIt(condition, AggregationTypeEnum.TERMS, returnName, column);
        });
        return typedThis;
    }

    @Override
    public Children termsAggregation(boolean condition, String returnName, R column) {
        return doIt(condition, AggregationTypeEnum.TERMS, returnName, column);
    }

    @Override
    public Children avg(boolean condition, String returnName, R column) {
        return doIt(condition, AggregationTypeEnum.AVG, returnName, column);
    }

    @Override
    public Children min(boolean condition, String returnName, R column) {
        return doIt(condition, AggregationTypeEnum.MIN, returnName, column);
    }

    @Override
    public Children max(boolean condition, String returnName, R column) {
        return doIt(condition, AggregationTypeEnum.MAX, returnName, column);
    }

    @Override
    public Children sum(boolean condition, String returnName, R column) {
        return doIt(condition, AggregationTypeEnum.SUM, returnName, column);
    }

    /**
     * 子类返回一个自己的新对象
     *
     * @return
     */
    protected abstract Children instance();

    /**
     * 封装查询参数 聚合类
     *
     * @param condition
     * @param aggregationTypeEnum
     * @param returnName
     * @param column
     * @return
     */
    private Children doIt(boolean condition, AggregationTypeEnum aggregationTypeEnum, String returnName, R column) {
        if (condition) {
            AggregationParam aggregationParam = new AggregationParam();
            aggregationParam.setName(returnName);
            aggregationParam.setField(FieldUtils.getFieldName(column));
            aggregationParam.setAggregationType(aggregationTypeEnum);
            aggregationParamList.add(aggregationParam);
        }
        return typedThis;
    }

    /**
     * 封装查询参数(含AND,OR这种连接操作)
     *
     * @param condition
     * @param func
     * @param open
     * @param close
     */
    private Children doIt(boolean condition, Function<Children, Children> func, BaseEsParamTypeEnum open, BaseEsParamTypeEnum close) {
        if (condition) {
            BaseEsParam left = new BaseEsParam();
            left.setType(open.getType());
            baseEsParamList.add(left);
            func.apply(instance());
            BaseEsParam right = new BaseEsParam();
            right.setType(close.getType());
            baseEsParamList.add(right);
        }
        return typedThis;
    }

    /**
     * 封装查询参数(普通情况,不带括号)
     *
     * @param condition
     * @param field
     * @param values
     * @param boost
     */
    private Children doIt(boolean condition, EsAttachTypeEnum attachTypeEnum, String field, List<Object> values, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .values(values)
                            .boost(boost)
                            .esQueryType(TERMS_QUERY.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 封装查询参数(普通情况,不带括号)
     *
     * @param condition
     * @param queryTypeEnum
     * @param attachTypeEnum
     * @param field
     * @param val
     * @param boost
     * @return
     */
    private Children doIt(boolean condition, EsQueryTypeEnum queryTypeEnum, EsAttachTypeEnum attachTypeEnum, String field, Object val, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .value(val)
                            .boost(boost)
                            .esQueryType(queryTypeEnum.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 封装查询参数针对is Null / not null 这类无值操作
     *
     * @param condition
     * @param attachTypeEnum
     * @param field
     * @param boost
     */
    private Children doIt(boolean condition, EsAttachTypeEnum attachTypeEnum, String field, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .boost(boost)
                            .esQueryType(EXISTS_QUERY.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 仅针对between的情况
     *
     * @param condition
     * @param attachTypeEnum
     * @param field
     * @param left
     * @param right
     * @param boost
     * @return
     */
    private Children doIt(boolean condition, EsAttachTypeEnum attachTypeEnum, String field, Object left, Object right, Float boost) {
        if (condition) {
            BaseEsParam baseEsParam = new BaseEsParam();
            BaseEsParam.FieldValueModel model =
                    BaseEsParam.FieldValueModel
                            .builder()
                            .field(field)
                            .leftValue(left)
                            .rightValue(right)
                            .boost(boost)
                            .esQueryType(INTERVAL_QUERY.getType())
                            .originalAttachType(attachTypeEnum.getType())
                            .build();

            setModel(baseEsParam, model, attachTypeEnum);
            baseEsParamList.add(baseEsParam);
        }
        return typedThis;
    }

    /**
     * 设置查询模型类型
     *
     * @param baseEsParam
     * @param model
     * @param attachTypeEnum
     */
    private void setModel(BaseEsParam baseEsParam, BaseEsParam.FieldValueModel model, EsAttachTypeEnum attachTypeEnum) {
        switch (attachTypeEnum) {
            case MUST:
                baseEsParam.getMustList().add(model);
                break;
            case FILTER:
                baseEsParam.getFilterList().add(model);
                break;
            case SHOULD:
                baseEsParam.getShouldList().add(model);
                break;
            case MUST_NOT:
                baseEsParam.getMustNotList().add(model);
                break;
            case GT:
                baseEsParam.getGtList().add(model);
                break;
            case LT:
                baseEsParam.getLtList().add(model);
                break;
            case GE:
                baseEsParam.getGeList().add(model);
                break;
            case LE:
                baseEsParam.getLeList().add(model);
                break;
            case IN:
                baseEsParam.getInList().add(model);
                break;
            case NOT_IN:
                baseEsParam.getNotInList().add(model);
                break;
            case EXISTS:
                baseEsParam.getNotNullList().add(model);
                break;
            case NOT_EXISTS:
                baseEsParam.getIsNullList().add(model);
                break;
            case BETWEEN:
                baseEsParam.getBetweenList().add(model);
                break;
            case NOT_BETWEEN:
                baseEsParam.getNotBetweenList().add(model);
            case LIKE_LEFT:
                baseEsParam.getLikeLeftList().add(model);
                break;
            case LIKE_RIGHT:
                baseEsParam.getLikeRightList().add(model);
                break;
            default:
                throw new UnsupportedOperationException("不支持的连接类型,请参见EsAttachTypeEnum");
        }
    }

}
