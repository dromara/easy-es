package com.xpc.easyes.core.conditions.interfaces;

import org.elasticsearch.index.query.Operator;

import java.io.Serializable;

import static com.xpc.easyes.core.constants.BaseEsConstants.*;

/**
 * 比较相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Compare<Children, R> extends Serializable {
    default Children eq(R column, Object val) {
        return eq(true, column, val);
    }

    default Children eq(R column, Object val, Float boost) {
        return eq(true, column, val, boost);
    }

    default Children eq(boolean condition, R column, Object val) {
        return eq(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * 等于
     *
     * @param condition 是否执行的条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children eq(boolean condition, R column, Object val, Float boost);

    default Children ne(R column, Object val) {
        return ne(true, column, val);
    }

    default Children ne(R column, Object val, Float boost) {
        return ne(true, column, val, boost);
    }

    default Children ne(boolean condition, R column, Object val) {
        return ne(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * 不等于
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重值
     * @return 泛型
     */
    Children ne(boolean condition, R column, Object val, Float boost);

    default Children match(R column, Object val) {
        return match(true, column, val);
    }

    default Children match(R column, Object val, Float boost) {
        return match(true, column, val, boost);
    }

    default Children match(boolean condition, R column, Object val) {
        return match(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * match 分词匹配
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重值
     * @return 泛型
     */
    Children match(boolean condition, R column, Object val, Float boost);


    default Children matchPhase(R column, Object val) {
        return matchPhase(true, column, val, DEFAULT_BOOST);
    }

    default Children matchPhase(boolean condition, R colmun, Object val) {
        return matchPhase(condition, colmun, val, DEFAULT_BOOST);
    }

    default Children matchPhase(R column, Object val, Float boost) {
        return matchPhase(true, column, val, boost);
    }

    /**
     * 分词匹配 需要结果中也包含所有的分词，且顺序一样
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重值
     * @return 泛型
     */
    Children matchPhase(boolean condition, R column, Object val, Float boost);


    default Children matchAllQuery() {
        return matchAllQuery(true);
    }

    /**
     * 查询全部文档
     *
     * @param condition 条件
     * @return 泛型
     */
    Children matchAllQuery(boolean condition);


    default Children matchPhrasePrefixQuery(R column, Object val) {
        return matchPhrasePrefixQuery(true, column, val, DEFAULT_MAX_EXPANSIONS, DEFAULT_BOOST);
    }

    default Children matchPhrasePrefixQuery(boolean condition, R column, Object val) {
        return matchPhrasePrefixQuery(condition, column, val, DEFAULT_MAX_EXPANSIONS, DEFAULT_BOOST);
    }

    default Children matchPhrasePrefixQuery(R column, Object val, Float boost) {
        return matchPhrasePrefixQuery(true, column, val, DEFAULT_MAX_EXPANSIONS, boost);
    }

    default Children matchPhrasePrefixQuery(R column, Object val, int maxExpansions) {
        return matchPhrasePrefixQuery(true, column, val, maxExpansions, DEFAULT_BOOST);
    }

    default Children matchPhrasePrefixQuery(R column, Object val, int maxExpansions, Float boost) {
        return matchPhrasePrefixQuery(true, column, val, maxExpansions, boost);
    }

    /**
     * 前缀匹配
     *
     * @param condition     条件
     * @param column        列
     * @param val           值
     * @param maxExpansions 最大扩展数
     * @param boost         权重值
     * @return 泛型
     */
    Children matchPhrasePrefixQuery(boolean condition, R column, Object val, int maxExpansions, Float boost);


    default Children multiMatchQuery(Object val, R... columns) {
        return multiMatchQuery(true, val, columns);
    }

    default Children multiMatchQuery(boolean condition, Object val, R... columns) {
        return multiMatchQuery(condition, val, Operator.OR, DEFAULT_MIN_SHOULD_MATCH, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, Float boost, R... columns) {
        return multiMatchQuery(true, val, Operator.OR, DEFAULT_MIN_SHOULD_MATCH, boost, columns);
    }

    default Children multiMatchQuery(Object val, int minimumShouldMatch, R... columns) {
        return multiMatchQuery(true, val, Operator.OR, minimumShouldMatch, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, int minimumShouldMatch, R... columns) {
        return multiMatchQuery(true, val, operator, minimumShouldMatch, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, R... columns) {
        return multiMatchQuery(true, val, operator, DEFAULT_MIN_SHOULD_MATCH, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, int minimumShouldMatch, Float boost, R... columns) {
        return multiMatchQuery(true, val, Operator.OR, minimumShouldMatch, boost, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, Float boost, R... columns) {
        return multiMatchQuery(true, val, operator, DEFAULT_MIN_SHOULD_MATCH, boost, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, int minimumShouldMatch, Float boost, R... columns) {
        return multiMatchQuery(true, val, operator, minimumShouldMatch, boost, columns);
    }

    /**
     * 多字段匹配
     *
     * @param condition          条件
     * @param val                值
     * @param boost              权重
     * @param columns            字段列表
     * @param operator           操作类型 or and
     * @param minimumShouldMatch 最小匹配度 百分比
     * @return 泛型
     */
    Children multiMatchQuery(boolean condition, Object val, Operator operator, int minimumShouldMatch, Float boost, R... columns);


    default Children queryStringQuery(String queryString) {
        return queryStringQuery(true, queryString, DEFAULT_BOOST);
    }

    default Children queryStringQuery(String queryString, Float boost) {
        return queryStringQuery(true, queryString, boost);
    }

    /**
     * 所有字段中搜索
     *
     * @param condition   条件
     * @param queryString 查询内容
     * @param boost       权重值
     * @return 泛型
     */
    Children queryStringQuery(boolean condition, String queryString, Float boost);


    default Children prefixQuery(R column, String prefix) {
        return prefixQuery(true, column, prefix);
    }

    default Children prefixQuery(boolean condition, R column, String prefix) {
        return prefixQuery(true, column, prefix, DEFAULT_BOOST);
    }

    default Children prefixQuery(R column, String prefix, Float boost) {
        return prefixQuery(true, column, prefix, boost);
    }

    /**
     * 前缀匹配搜索
     *
     * @param condition 条件
     * @param column    列
     * @param prefix    前缀
     * @param boost     权重值
     * @return 泛型
     */
    Children prefixQuery(boolean condition, R column, String prefix, Float boost);


    default Children notMatch(R column, Object val) {
        return notMatch(true, column, val);
    }

    default Children notMatch(R column, Object val, Float boost) {
        return notMatch(true, column, val, boost);
    }

    default Children notMatch(boolean condition, R column, Object val) {
        return notMatch(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * NOT MATCH 分词不匹配
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children notMatch(boolean condition, R column, Object val, Float boost);

    default Children gt(R column, Object val) {
        return gt(true, column, val);
    }

    default Children gt(R column, Object val, Float boost) {
        return gt(true, column, val, boost);
    }

    default Children gt(boolean condition, R column, Object val) {
        return gt(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * 大于
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children gt(boolean condition, R column, Object val, Float boost);

    default Children ge(R column, Object val) {
        return ge(true, column, val);
    }

    default Children ge(R column, Object val, Float boost) {
        return ge(true, column, val, boost);
    }

    default Children ge(boolean condition, R column, Object val) {
        return ge(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * 大于等于
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children ge(boolean condition, R column, Object val, Float boost);

    default Children lt(R column, Object val) {
        return lt(true, column, val);
    }

    default Children lt(R column, Object val, Float boost) {
        return lt(true, column, val, boost);
    }

    default Children lt(boolean condition, R column, Object val) {
        return lt(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * 小于
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children lt(boolean condition, R column, Object val, Float boost);

    default Children le(R column, Object val) {
        return le(true, column, val);
    }

    default Children le(R column, Object val, Float boost) {
        return le(true, column, val, boost);
    }

    default Children le(boolean condition, R column, Object val) {
        return le(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * 小于等于
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children le(boolean condition, R column, Object val, Float boost);

    default Children between(R column, Object val1, Object val2) {
        return between(true, column, val1, val2);
    }

    default Children between(R column, Object val1, Object val2, Float boost) {
        return between(true, column, val1, val2, boost);
    }

    default Children between(boolean condition, R column, Object val1, Object val2) {
        return between(condition, column, val1, val2, DEFAULT_BOOST);
    }

    /**
     * BETWEEN 值1 AND 值2
     *
     * @param condition 条件
     * @param column    列
     * @param val1      左区间值
     * @param val2      右区间值
     * @param boost     权重
     * @return 泛型
     */
    Children between(boolean condition, R column, Object val1, Object val2, Float boost);


    default Children notBetween(R column, Object val1, Object val2) {
        return notBetween(true, column, val1, val2);
    }

    default Children notBetween(R column, Object val1, Object val2, Float boost) {
        return notBetween(true, column, val1, val2, boost);
    }

    default Children notBetween(boolean condition, R column, Object val1, Object val2) {
        return notBetween(condition, column, val1, val2, DEFAULT_BOOST);
    }

    /**
     * NOT BETWEEN 值1 AND 值2
     *
     * @param condition 条件
     * @param column    列
     * @param val1      左区间值
     * @param val2      右区间值
     * @param boost     权重
     * @return 泛型
     */
    Children notBetween(boolean condition, R column, Object val1, Object val2, Float boost);

    default Children like(R column, Object val) {
        return like(true, column, val);
    }

    default Children like(R column, Object val, Float boost) {
        return like(true, column, val, boost);
    }

    default Children like(boolean condition, R column, Object val) {
        return like(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * like 左右皆模糊
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children like(boolean condition, R column, Object val, Float boost);

    default Children notLike(R column, Object val) {
        return notLike(true, column, val);
    }

    default Children notLike(R column, Object val, Float boost) {
        return notLike(true, column, val, boost);
    }

    default Children notLike(boolean condition, R column, Object val) {
        return notLike(condition, column, val, DEFAULT_BOOST);
    }

    /**
     * NOT LIKE
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children notLike(boolean condition, R column, Object val, Float boost);

    default Children likeLeft(R column, Object val) {
        return likeLeft(true, column, val, DEFAULT_BOOST);
    }

    default Children likeLeft(R column, Object val, Float boost) {
        return likeLeft(true, column, val, boost);
    }

    /**
     * LIKE左模糊
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children likeLeft(boolean condition, R column, Object val, Float boost);


    default Children likeRight(R column, Object val) {
        return likeRight(true, column, val, DEFAULT_BOOST);
    }


    default Children likeRight(R column, Object val, Float boost) {
        return likeRight(true, column, val, boost);
    }

    /**
     * LIKE右模糊
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @param boost     权重
     * @return 泛型
     */
    Children likeRight(boolean condition, R column, Object val, Float boost);
}
