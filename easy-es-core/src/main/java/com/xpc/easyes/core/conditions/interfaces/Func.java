package com.xpc.easyes.core.conditions.interfaces;

import com.xpc.easyes.core.common.OrderByParam;
import com.xpc.easyes.core.constants.BaseEsConstants;
import com.xpc.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * 高阶语法相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("unchecked")
public interface Func<Children, R> extends Serializable {
    default Children highLight(R column) {
        return highLight(true, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, column);
    }

    default Children highLight(boolean condition, R column) {
        return highLight(condition, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, column);
    }

    default Children highLight(String preTag, String postTag, R column) {
        return highLight(true, preTag, postTag, column);
    }

    /**
     * 高亮
     *
     * @param condition 是否执行条件
     * @param preTag    高亮的开始标签
     * @param postTag   高亮的结束标签
     * @param column    列
     * @return 泛型
     */
    Children highLight(boolean condition, String preTag, String postTag, R column);

    default Children highLight(R... columns) {
        return highLight(true, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, columns);
    }

    default Children highLight(boolean condition, R... columns) {
        return highLight(condition, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, columns);
    }

    /**
     * 高亮
     *
     * @param condition 是否执行条件
     * @param preTag    高亮的开始标签
     * @param postTag   高亮的结束标签
     * @param columns   列,支持多列
     * @return 泛型
     */
    Children highLight(boolean condition, String preTag, String postTag, R... columns);


    default Children orderByAsc(R column) {
        return orderByAsc(true, column);
    }

    default Children orderByAsc(R... columns) {
        return orderByAsc(true, columns);
    }

    default Children orderByAsc(boolean condition, R... columns) {
        return orderBy(condition, true, columns);
    }

    default Children orderByDesc(R column) {
        return orderByDesc(true, column);
    }

    default Children orderByDesc(R... columns) {
        return orderByDesc(true, columns);
    }

    default Children orderByDesc(boolean condition, R... columns) {
        return orderBy(condition, false, columns);
    }

    /**
     * 排序：ORDER BY 字段, ...
     *
     * @param condition 条件
     * @param isAsc     是否升序 是:按照升序排列,否:安卓降序排列
     * @param columns   列,支持多列
     * @return 泛型
     */
    Children orderBy(boolean condition, boolean isAsc, R... columns);


    default Children orderBy(OrderByParam orderByParam) {
        return orderBy(true, orderByParam);
    }

    default Children orderBy(boolean condition, OrderByParam orderByParam) {
        return orderBy(condition, Collections.singletonList(orderByParam));
    }

    default Children orderBy(List<OrderByParam> orderByParams) {
        return orderBy(true, orderByParams);
    }

    /**
     * 排序 适用于排序字段和规则从前端通过字符串传入的场景
     *
     * @param condition     条件
     * @param orderByParams 排序字段及规则参数列表
     * @return 泛型
     */
    Children orderBy(boolean condition, List<OrderByParam> orderByParams);

    default Children in(R column, Collection<?> coll) {
        return in(true, column, coll);
    }

    default Children in(boolean condition, R column, Collection<?> coll) {
        return in(condition, column, coll, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 字段 IN
     *
     * @param condition 条件
     * @param column    列
     * @param coll      集合
     * @param boost     权重
     * @return 泛型
     */
    Children in(boolean condition, R column, Collection<?> coll, Float boost);

    default Children in(R column, Object... values) {
        return in(true, column, values);
    }

    default Children in(boolean condition, R column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    default Children notIn(R column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    default Children notIn(boolean condition, R column, Collection<?> coll) {
        return notIn(condition, column, coll, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 字段 NOT IN
     *
     * @param condition 条件
     * @param column    列
     * @param coll      集合
     * @param boost     权重
     * @return 泛型
     */
    Children notIn(boolean condition, R column, Collection<?> coll, Float boost);

    default Children notIn(R column, Object... value) {
        return notIn(true, column, value);
    }

    default Children notIn(boolean condition, R column, Object... values) {
        return notIn(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    default Children isNull(R column) {
        return isNull(true, column);
    }

    default Children isNull(boolean condition, R column) {
        return isNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 字段 IS NULL
     *
     * @param condition 条件
     * @param column    列
     * @param boost     权重
     * @return 泛型
     */
    Children isNull(boolean condition, R column, Float boost);

    default Children isNotNull(R column) {
        return isNotNull(true, column);
    }

    default Children isNotNull(boolean condition, R column) {
        return isNotNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    /***
     * 字段 IS NOT NULL
     * @param condition 条件
     * @param column 列
     * @param boost 权重
     * @return 泛型
     */
    Children isNotNull(boolean condition, R column, Float boost);

    default Children groupBy(R column) {
        return groupBy(true, true, column);
    }

    default Children groupBy(boolean enablePipeline, R column) {
        return groupBy(true, enablePipeline, column);
    }

    default Children groupBy(R... columns) {
        return groupBy(true, true, columns);
    }

    default Children groupBy(boolean enablePipeline, R... columns) {
        return groupBy(true, enablePipeline, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param condition 条件
     * @param enablePipeline  是否管道聚合
     * @param columns   列,支持多列
     * @return 泛型
     */
    Children groupBy(boolean condition, boolean enablePipeline, R... columns);

    default Children termsAggregation(R column) {
        return termsAggregation(true, true, FieldUtils.getFieldName(column), column);
    }

    default Children termsAggregation(boolean enablePipeline, R column) {
        return termsAggregation(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children termsAggregation(String returnName, R column) {
        return termsAggregation(true, true, returnName, column);
    }

    default Children termsAggregation(boolean enablePipeline, String returnName, R column) {
        return termsAggregation(true, enablePipeline, returnName, column);
    }

    /**
     * 可指定返回名称分组,相当于mysql group by
     *
     * @param condition  条件
     * @param enablePipeline   是否管道聚合
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children termsAggregation(boolean condition, boolean enablePipeline, String returnName, R column);

    default Children avg(R column) {
        return avg(true, true, FieldUtils.getFieldName(column), column);
    }

    default Children avg(boolean enablePipeline, R column) {
        return avg(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children avg(boolean enablePipeline, String returnName, R column) {
        return avg(true, enablePipeline, returnName, column);
    }

    default Children avg(String returnName, R column) {
        return avg(true, true, returnName, column);
    }

    /**
     * 求平均值
     *
     * @param condition  条件
     * @param enablePipeline   是否管道聚合
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children avg(boolean condition, boolean enablePipeline, String returnName, R column);

    default Children min(R column) {
        return min(true, FieldUtils.getFieldName(column), column);
    }

    default Children min(boolean enablePipeline, R column) {
        return min(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children min(boolean enablePipeline, String returnName, R column) {
        return min(true, enablePipeline, returnName, column);
    }

    default Children min(String returnName, R column) {
        return min(true, true, returnName, column);
    }

    /**
     * 求最小值
     *
     * @param condition  条件
     * @param enablePipeline   是否管道聚合
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children min(boolean condition, boolean enablePipeline, String returnName, R column);

    default Children max(R column) {
        return max(true, FieldUtils.getFieldName(column), column);
    }

    default Children max(boolean enablePipeline, R column) {
        return max(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children max(boolean enablePipeline, String returnName, R column) {
        return max(true, enablePipeline, returnName, column);
    }

    default Children max(String returnName, R column) {
        return max(true, true, returnName, column);
    }

    /**
     * 求最大值
     *
     * @param condition  条件
     * @param enablePipeline   是否管道聚合
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children max(boolean condition, boolean enablePipeline, String returnName, R column);

    default Children sum(R column) {
        return sum(true, FieldUtils.getFieldName(column), column);
    }

    default Children sum(boolean enablePipeline, R column) {
        return sum(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children sum(boolean enablePipeline, String returnName, R column) {
        return sum(true, enablePipeline, returnName, column);
    }

    default Children sum(String returnName, R column) {
        return sum(true, true, returnName, column);
    }

    /**
     * 求和
     *
     * @param condition  条件
     * @param enablePipeline   是否管道聚合
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children sum(boolean condition, boolean enablePipeline, String returnName, R column);


    default Children sort(SortBuilder<?> sortBuilder) {
        return sort(true, sortBuilder);
    }

    default Children sort(boolean condition, SortBuilder<?> sortBuilder) {
        return sort(condition, Collections.singletonList(sortBuilder));
    }

    /**
     * 用户自定义排序
     *
     * @param condition    条件
     * @param sortBuilders 排序规则列表
     * @return 泛型
     */
    Children sort(boolean condition, List<SortBuilder<?>> sortBuilders);

    /**
     * 根据得分_score排序 默认为降序 得分高得在前
     *
     * @return 泛型
     */
    default Children sortByScore() {
        return sortByScore(true, SortOrder.DESC);
    }

    /**
     * 根据得分_score排序 默认为降序 得分高得在前
     *
     * @param condition 条件
     * @return 泛型
     */
    default Children sortByScore(boolean condition) {
        return sortByScore(condition, SortOrder.DESC);
    }

    default Children sortByScore(SortOrder sortOrder) {
        return sortByScore(true, sortOrder);
    }

    /**
     * 根据得分_score排序
     *
     * @param condition 条件
     * @param sortOrder 升序/降序
     * @return 泛型
     */
    Children sortByScore(boolean condition, SortOrder sortOrder);


    default Children distinct(R column) {
        return distinct(true, column);
    }

    /**
     * 单字段去重
     *
     * @param condition 条件
     * @param column    去重字段
     * @return 泛型
     */
    Children distinct(boolean condition, R column);
}
