package com.xpc.easyes.core.conditions.interfaces;

import com.xpc.easyes.core.constants.BaseEsConstants;
import com.xpc.easyes.core.toolkit.FieldUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

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

    default Children groupBy(R... columns) {
        return groupBy(true, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param condition 条件
     * @param columns   列,支持多列
     * @return 泛型
     */
    Children groupBy(boolean condition, R... columns);

    default Children termsAggregation(R column) {
        return termsAggregation(true, FieldUtils.getFieldName(column), column);
    }

    default Children termsAggregation(String returnName, R column) {
        return termsAggregation(true, returnName, column);
    }

    /**
     * 可指定返回名称分组,相当于mysql group by
     *
     * @param condition  条件
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children termsAggregation(boolean condition, String returnName, R column);

    default Children avg(R column) {
        return avg(true, FieldUtils.getFieldName(column), column);
    }

    default Children avg(String returnName, R column) {
        return avg(true, returnName, column);
    }

    /**
     * 求平均值
     *
     * @param condition  条件
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children avg(boolean condition, String returnName, R column);

    default Children min(R column) {
        return min(true, FieldUtils.getFieldName(column), column);
    }

    default Children min(String returnName, R column) {
        return min(true, returnName, column);
    }

    /**
     * 求最小值
     *
     * @param condition  条件
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children min(boolean condition, String returnName, R column);

    default Children max(R column) {
        return max(true, FieldUtils.getFieldName(column), column);
    }

    default Children max(String returnName, R column) {
        return max(true, returnName, column);
    }

    /**
     * 求最大值
     *
     * @param condition  条件
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children max(boolean condition, String returnName, R column);

    default Children sum(R column) {
        return sum(true, FieldUtils.getFieldName(column), column);
    }

    default Children sum(String returnName, R column) {
        return sum(true, returnName, column);
    }

    /**
     * 求和
     *
     * @param condition  条件
     * @param returnName 返回的聚合字段名称
     * @param column     列
     * @return 泛型
     */
    Children sum(boolean condition, String returnName, R column);

}
