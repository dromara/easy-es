package cn.easyes.core.conditions.interfaces;

import cn.easyes.common.constants.BaseEsConstants;
import cn.easyes.core.biz.OrderByParam;
import cn.easyes.core.toolkit.FieldUtils;
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

    default Children orderByAsc(String column) {
        return orderByAsc(true, column);
    }

    default Children orderByAsc(String... columns) {
        return orderByAsc(true, columns);
    }

    default Children orderByAsc(boolean condition, String... columns) {
        return orderBy(condition, true, columns);
    }

    default Children orderByDesc(String column) {
        return orderByDesc(true, column);
    }

    default Children orderByDesc(String... columns) {
        return orderByDesc(true, columns);
    }

    default Children orderByDesc(boolean condition, String... columns) {
        return orderBy(condition, false, columns);
    }

    default Children orderBy(boolean condition, boolean isAsc, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return orderBy(condition, isAsc, fields);
    }


    /**
     * 排序：ORDER BY 字段, ...
     *
     * @param condition 条件
     * @param isAsc     是否升序 是:按照升序排列,否:安卓降序排列
     * @param columns   列,支持多列
     * @return 泛型
     */
    Children orderBy(boolean condition, boolean isAsc, String... columns);


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

    default Children in(R column, Object... values) {
        return in(true, column, values);
    }

    default Children in(boolean condition, R column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    default Children in(String column, Collection<?> coll) {
        return in(true, column, coll);
    }

    default Children in(boolean condition, String column, Collection<?> coll) {
        return in(condition, column, coll, BaseEsConstants.DEFAULT_BOOST);
    }

    default Children in(String column, Object... values) {
        return in(true, column, values);
    }

    default Children in(boolean condition, String column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    default Children in(boolean condition, R column, Collection<?> coll, Float boost) {
        return in(condition, FieldUtils.getFieldName(column), coll, boost);
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
    Children in(boolean condition, String column, Collection<?> coll, Float boost);


    default Children notIn(R column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    default Children notIn(boolean condition, R column, Collection<?> coll) {
        return notIn(condition, column, coll, BaseEsConstants.DEFAULT_BOOST);
    }

    default Children notIn(R column, Object... value) {
        return notIn(true, column, value);
    }

    default Children notIn(boolean condition, R column, Object... values) {
        return notIn(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    default Children notIn(String column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    default Children notIn(boolean condition, String column, Collection<?> coll) {
        return notIn(condition, column, coll, BaseEsConstants.DEFAULT_BOOST);
    }

    default Children notIn(String column, Object... value) {
        return notIn(true, column, value);
    }

    default Children notIn(boolean condition, String column, Object... values) {
        return notIn(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    default Children notIn(boolean condition, R column, Collection<?> coll, Float boost) {
        return notIn(condition, FieldUtils.getFieldName(column), coll, boost);
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
    Children notIn(boolean condition, String column, Collection<?> coll, Float boost);

    default Children isNull(R column) {
        return isNull(true, column);
    }

    default Children isNull(boolean condition, R column) {
        return isNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    default Children isNull(String column) {
        return isNull(true, column);
    }

    default Children isNull(boolean condition, String column) {
        return isNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    default Children isNull(boolean condition, R column, Float boost) {
        return isNull(condition, FieldUtils.getFieldName(column), boost);
    }


    /**
     * 字段 IS NULL
     *
     * @param condition 条件
     * @param column    列
     * @param boost     权重
     * @return 泛型
     */
    Children isNull(boolean condition, String column, Float boost);

    default Children isNotNull(R column) {
        return isNotNull(true, column);
    }

    default Children isNotNull(boolean condition, R column) {
        return isNotNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    default Children isNotNull(String column) {
        return isNotNull(true, column);
    }

    default Children isNotNull(boolean condition, String column) {
        return isNotNull(condition, column, BaseEsConstants.DEFAULT_BOOST);
    }

    default Children isNotNull(boolean condition, R column, Float boost) {
        return isNotNull(condition, FieldUtils.getFieldName(column), boost);
    }

    /***
     * 字段 IS NOT NULL
     * @param condition 条件
     * @param column 列
     * @param boost 权重
     * @return 泛型
     */
    Children isNotNull(boolean condition, String column, Float boost);

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

    default Children groupBy(String column) {
        return groupBy(true, true, column);
    }

    default Children groupBy(boolean enablePipeline, String column) {
        return groupBy(true, enablePipeline, column);
    }

    default Children groupBy(String... columns) {
        return groupBy(true, true, columns);
    }

    default Children groupBy(boolean enablePipeline, String... columns) {
        return groupBy(true, enablePipeline, columns);
    }

    default Children groupBy(boolean condition, boolean enablePipeline, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return groupBy(condition, enablePipeline, fields);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param condition      条件
     * @param enablePipeline 是否管道聚合
     * @param columns        列,支持多列
     * @return 泛型
     */
    Children groupBy(boolean condition, boolean enablePipeline, String... columns);

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

    default Children termsAggregation(String column) {
        return termsAggregation(true, true, FieldUtils.getFieldName(column), column);
    }

    default Children termsAggregation(boolean enablePipeline, String column) {
        return termsAggregation(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children termsAggregation(String returnName, String column) {
        return termsAggregation(true, true, returnName, column);
    }

    default Children termsAggregation(boolean enablePipeline, String returnName, String column) {
        return termsAggregation(true, enablePipeline, returnName, column);
    }

    default Children termsAggregation(boolean condition, boolean enablePipeline, String returnName, R column) {
        return termsAggregation(condition, enablePipeline, returnName, FieldUtils.getFieldName(column));
    }

    /**
     * 可指定返回名称分组,相当于mysql group by
     *
     * @param condition      条件
     * @param enablePipeline 是否管道聚合
     * @param returnName     返回的聚合字段名称
     * @param column         列
     * @return 泛型
     */
    Children termsAggregation(boolean condition, boolean enablePipeline, String returnName, String column);


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

    default Children avg(String column) {
        return avg(true, true, FieldUtils.getFieldName(column), column);
    }

    default Children avg(boolean enablePipeline, String column) {
        return avg(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children avg(boolean enablePipeline, String returnName, String column) {
        return avg(true, enablePipeline, returnName, column);
    }

    default Children avg(String returnName, String column) {
        return avg(true, true, returnName, column);
    }


    default Children avg(boolean condition, boolean enablePipeline, String returnName, R column) {
        return avg(condition, enablePipeline, returnName, FieldUtils.getFieldName(column));
    }

    /**
     * 求平均值
     *
     * @param condition      条件
     * @param enablePipeline 是否管道聚合
     * @param returnName     返回的聚合字段名称
     * @param column         列
     * @return 泛型
     */
    Children avg(boolean condition, boolean enablePipeline, String returnName, String column);

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

    default Children min(String column) {
        return min(true, FieldUtils.getFieldName(column), column);
    }

    default Children min(boolean enablePipeline, String column) {
        return min(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children min(boolean enablePipeline, String returnName, String column) {
        return min(true, enablePipeline, returnName, column);
    }

    default Children min(String returnName, String column) {
        return min(true, true, returnName, column);
    }

    default Children min(boolean condition, boolean enablePipeline, String returnName, R column) {
        return min(condition, enablePipeline, returnName, FieldUtils.getFieldName(column));
    }

    /**
     * 求最小值
     *
     * @param condition      条件
     * @param enablePipeline 是否管道聚合
     * @param returnName     返回的聚合字段名称
     * @param column         列
     * @return 泛型
     */
    Children min(boolean condition, boolean enablePipeline, String returnName, String column);


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

    default Children max(String column) {
        return max(true, FieldUtils.getFieldName(column), column);
    }

    default Children max(boolean enablePipeline, String column) {
        return max(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children max(boolean enablePipeline, String returnName, String column) {
        return max(true, enablePipeline, returnName, column);
    }

    default Children max(String returnName, String column) {
        return max(true, true, returnName, column);
    }

    default Children max(boolean condition, boolean enablePipeline, String returnName, R column) {
        return max(condition, enablePipeline, returnName, FieldUtils.getFieldName(column));
    }

    /**
     * 求最大值
     *
     * @param condition      条件
     * @param enablePipeline 是否管道聚合
     * @param returnName     返回的聚合字段名称
     * @param column         列
     * @return 泛型
     */
    Children max(boolean condition, boolean enablePipeline, String returnName, String column);


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

    default Children sum(String column) {
        return sum(true, FieldUtils.getFieldName(column), column);
    }

    default Children sum(boolean enablePipeline, String column) {
        return sum(true, enablePipeline, FieldUtils.getFieldName(column), column);
    }

    default Children sum(boolean enablePipeline, String returnName, String column) {
        return sum(true, enablePipeline, returnName, column);
    }

    default Children sum(String returnName, String column) {
        return sum(true, true, returnName, column);
    }

    default Children sum(boolean condition, boolean enablePipeline, String returnName, R column) {
        return sum(condition, enablePipeline, returnName, FieldUtils.getFieldName(column));
    }

    /**
     * 求和
     *
     * @param condition      条件
     * @param enablePipeline 是否管道聚合
     * @param returnName     返回的聚合字段名称
     * @param column         列
     * @return 泛型
     */
    Children sum(boolean condition, boolean enablePipeline, String returnName, String column);


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

    default Children distinct(String column) {
        return distinct(true, column);
    }

    default Children distinct(boolean condition, R column) {
        return distinct(condition, FieldUtils.getFieldName(column));
    }

    /**
     * 单字段去重
     *
     * @param condition 条件
     * @param column    去重字段
     * @return 泛型
     */
    Children distinct(boolean condition, String column);
}
