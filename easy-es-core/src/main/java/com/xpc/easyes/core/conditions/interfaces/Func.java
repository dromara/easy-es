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
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 高阶语法相关参数都在此封装
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("unchecked")
public interface Func<Children, R> extends Serializable {
    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children highLight(R column) {
        return highLight(true, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, column);
    }

    /**
     * ignore
     *
     * @param condition
     * @param column
     * @return
     */
    default Children highLight(boolean condition, R column) {
        return highLight(condition, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, column);
    }

    /**
     * ignore
     *
     * @param preTag
     * @param postTag
     * @param column
     * @return
     */
    default Children highLight(String preTag, String postTag, R column) {
        return highLight(true, preTag, postTag, column);
    }

    /**
     * 高亮
     *
     * @param condition
     * @param preTag
     * @param postTag
     * @param column
     * @return
     */
    Children highLight(boolean condition, String preTag, String postTag, R column);

    /**
     * ignore
     *
     * @param columns
     * @return
     */
    default Children highLight(R... columns) {
        return highLight(true, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, columns);
    }

    /**
     * ignore
     *
     * @param condition
     * @param columns
     * @return
     */
    default Children highLight(boolean condition, R... columns) {
        return highLight(condition, BaseEsConstants.HIGH_LIGHT_PRE_TAG, BaseEsConstants.HIGH_LIGHT_POST_TAG, columns);
    }

    /**
     * 高亮
     *
     * @param condition
     * @param columns
     * @param preTag
     * @param postTag
     * @return
     */
    Children highLight(boolean condition, String preTag, String postTag, R... columns);


    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children orderByAsc(R column) {
        return orderByAsc(true, column);
    }

    /**
     * ignore
     *
     * @param columns
     * @return
     */
    default Children orderByAsc(R... columns) {
        return orderByAsc(true, columns);
    }

    /**
     * 排序：ORDER BY 字段, ... ASC
     * <p>例: orderByAsc("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    default Children orderByAsc(boolean condition, R... columns) {
        return orderBy(condition, true, columns);
    }

    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children orderByDesc(R column) {
        return orderByDesc(true, column);
    }

    /**
     * ignore
     *
     * @param columns
     * @return
     */
    default Children orderByDesc(R... columns) {
        return orderByDesc(true, columns);
    }

    /**
     * 排序：ORDER BY 字段, ... DESC
     * <p>例: orderByDesc("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    default Children orderByDesc(boolean condition, R... columns) {
        return orderBy(condition, false, columns);
    }

    /**
     * 排序：ORDER BY 字段, ...
     * <p>例: orderBy(true, "id", "name")</p>
     *
     * @param condition 执行条件
     * @param isAsc     是否是 ASC 排序
     * @param columns   字段数组
     * @return children
     */
    Children orderBy(boolean condition, boolean isAsc, R... columns);


    /**
     * ignore
     *
     * @param column
     * @param coll
     * @return
     */
    default Children in(R column, Collection<?> coll) {
        return in(true, column, coll);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    default Children in(boolean condition, R column, Collection<?> coll) {
        return in(condition, column, coll, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 如果集合为 empty 则不会进行 语句 拼接 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @param boost     权重值
     * @return children
     */
    Children in(boolean condition, R column, Collection<?> coll, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param values
     * @return
     */
    default Children in(R column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 字段 IN (v0, v1, ...)
     * <p>例: in("id", 1, 2, 3, 4, 5)</p>
     *
     * <li> 如果动态数组为 empty 则不会进行 语句 拼接 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    default Children in(boolean condition, R column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }


    /**
     * ignore
     *
     * @param column
     * @param coll
     * @return
     */
    default Children notIn(R column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    default Children notIn(boolean condition, R column, Collection<?> coll) {
        return notIn(condition, column, coll, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @param boost     权重值
     * @return children
     */
    Children notIn(boolean condition, R column, Collection<?> coll, Float boost);

    /**
     * ignore
     *
     * @param column
     * @param value
     * @return
     */
    default Children notIn(R column, Object... value) {
        return notIn(true, column, value);
    }

    /**
     * 字段 NOT IN (v0, v1, ...)
     * <p>例: notIn("id", 1, 2, 3, 4, 5)</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    default Children notIn(boolean condition, R column, Object... values) {
        return notIn(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }


    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children isNull(R column) {
        return isNull(true, column);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    default Children isNull(boolean condition, R column) {
        return isNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 字段 IS NULL
     * <p>例: isNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param boost     权重
     * @return children
     */
    Children isNull(boolean condition, R column, Float boost);

    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children isNotNull(R column) {
        return isNotNull(true, column);
    }

    /**
     * ignore
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    default Children isNotNull(boolean condition, R column) {
        return isNotNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    /**
     * 字段 IS NOT NULL
     * <p>例: isNotNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param boost     权重
     * @return children
     */
    Children isNotNull(boolean condition, R column, Float boost);

    /**
     * ignore
     *
     * @param columns
     * @return
     */
    default Children groupBy(R... columns) {
        return groupBy(true, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     * 不可指定返回字段,语法与mysql一致,按group by的字段名称返回
     * <p>例: groupBy("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    Children groupBy(boolean condition, R... columns);

    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children termsAggregation(R column) {
        return termsAggregation(true, FieldUtils.getFieldName(column), column);
    }

    /**
     * ignore
     *
     * @param returnName
     * @param column
     * @return
     */
    default Children termsAggregation(String returnName, R column) {
        return termsAggregation(true, returnName, column);
    }

    /**
     * 可指定返回名称分组,相当于mysql group by
     *
     * @param condition
     * @param returnName
     * @param column
     * @return
     */
    Children termsAggregation(boolean condition, String returnName, R column);

    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children avg(R column) {
        return avg(true, FieldUtils.getFieldName(column), column);
    }

    /**
     * ignore
     *
     * @param returnName
     * @param column
     * @return
     */
    default Children avg(String returnName, R column) {
        return avg(true, returnName, column);
    }

    /**
     * 求平均值
     *
     * @param condition  查询条件
     * @param returnName 返回字段名称
     * @param column     查询字段
     * @return
     */
    Children avg(boolean condition, String returnName, R column);

    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children min(R column) {
        return min(true, FieldUtils.getFieldName(column), column);
    }

    /**
     * ignore
     *
     * @param returnName
     * @param column
     * @return
     */
    default Children min(String returnName, R column) {
        return min(true, returnName, column);
    }

    /**
     * 求最小值
     *
     * @param condition  查询条件
     * @param returnName 返回字段名称
     * @param column     查询字段
     * @return
     */
    Children min(boolean condition, String returnName, R column);

    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children max(R column) {
        return max(true, FieldUtils.getFieldName(column), column);
    }

    /**
     * ignore
     *
     * @param returnName
     * @param column
     * @return
     */
    default Children max(String returnName, R column) {
        return max(true, returnName, column);
    }

    /**
     * 求最大值
     *
     * @param condition  查询条件
     * @param returnName 返回字段名称
     * @param column     查询字段
     * @return
     */
    Children max(boolean condition, String returnName, R column);

    /**
     * ignore
     *
     * @param column
     * @return
     */
    default Children sum(R column) {
        return sum(true, FieldUtils.getFieldName(column), column);
    }

    /**
     * ignore
     *
     * @param returnName
     * @param column
     * @return
     */
    default Children sum(String returnName, R column) {
        return sum(true, returnName, column);
    }

    /**
     * 求和
     *
     * @param condition  查询条件
     * @param returnName 返回字段名称
     * @param column     查询字段
     * @return
     */
    Children sum(boolean condition, String returnName, R column);

}
