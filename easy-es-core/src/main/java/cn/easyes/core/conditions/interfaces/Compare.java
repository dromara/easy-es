package cn.easyes.core.conditions.interfaces;

import cn.easyes.core.toolkit.FieldUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.Operator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;

import static cn.easyes.common.constants.BaseEsConstants.*;

/**
 * 比较相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Compare<Children, R> extends Serializable {

    default <V> Children allEq(Map<String, V> params) {
        return allEq(params, true);
    }


    default <V> Children allEq(Map<String, V> params, boolean null2IsNull) {
        return allEq(true, params, null2IsNull);
    }

    /**
     * map 所有非空属性等于 =
     *
     * @param condition   执行条件
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @param null2IsNull 是否参数为 null 自动执行 isNull 方法, false 则忽略这个字段
     * @param <V>         ignore
     * @return children
     */
    <V> Children allEq(boolean condition, Map<String, V> params, boolean null2IsNull);

    default <V> Children allEq(BiPredicate<String, V> filter, Map<String, V> params) {
        return allEq(filter, params, true);
    }

    default <V> Children allEq(BiPredicate<String, V> filter, Map<String, V> params, boolean null2IsNull) {
        return allEq(true, filter, params, null2IsNull);
    }

    /**
     * 字段过滤接口，传入多参数时允许对参数进行过滤
     *
     * @param condition   执行条件
     * @param filter      返回 true 来允许字段传入比对条件中
     * @param params      map 类型的参数, key 是字段名, value 是字段值
     * @param null2IsNull 是否参数为 null 自动执行 isNull 方法, false 则忽略这个字段
     * @param <V>         ignore
     * @return 泛型
     */
    <V> Children allEq(boolean condition, BiPredicate<String, V> filter, Map<String, V> params, boolean null2IsNull);

    default Children eq(R column, Object val) {
        return eq(true, column, val);
    }

    default Children eq(R column, Object val, Float boost) {
        return eq(true, column, val, boost);
    }

    default Children eq(boolean condition, R column, Object val) {
        return eq(condition, column, val, DEFAULT_BOOST);
    }

    default Children eq(String column, Object val) {
        return eq(true, column, val);
    }

    default Children eq(String column, Object val, Float boost) {
        return eq(true, column, val, boost);
    }

    default Children eq(boolean condition, String column, Object val) {
        return eq(condition, column, val, DEFAULT_BOOST);
    }

    default Children eq(boolean condition, R column, Object val, Float boost) {
        return eq(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children eq(boolean condition, String column, Object val, Float boost);

    default Children ne(R column, Object val) {
        return ne(true, column, val);
    }

    default Children ne(R column, Object val, Float boost) {
        return ne(true, column, val, boost);
    }

    default Children ne(boolean condition, R column, Object val) {
        return ne(condition, column, val, DEFAULT_BOOST);
    }

    default Children ne(String column, Object val) {
        return ne(true, column, val);
    }

    default Children ne(String column, Object val, Float boost) {
        return ne(true, column, val, boost);
    }

    default Children ne(boolean condition, String column, Object val) {
        return ne(condition, column, val, DEFAULT_BOOST);
    }

    default Children ne(boolean condition, R column, Object val, Float boost) {
        return ne(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children ne(boolean condition, String column, Object val, Float boost);

    default Children match(R column, Object val) {
        return match(true, column, val);
    }

    default Children match(R column, Object val, Float boost) {
        return match(true, column, val, boost);
    }

    default Children match(boolean condition, R column, Object val) {
        return match(condition, column, val, DEFAULT_BOOST);
    }

    default Children match(String column, Object val) {
        return match(true, column, val);
    }

    default Children match(String column, Object val, Float boost) {
        return match(true, column, val, boost);
    }

    default Children match(boolean condition, String column, Object val) {
        return match(condition, column, val, DEFAULT_BOOST);
    }

    default Children match(boolean condition, R column, Object val, Float boost) {
        return match(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children match(boolean condition, String column, Object val, Float boost);


    default Children nestedMatch(R path, String column, Object val) {
        return nestedMatch(true, path, column, val, ScoreMode.Avg, DEFAULT_BOOST);
    }

    default Children nestedMatch(R path, String column, Object val, ScoreMode scoreMode) {
        return nestedMatch(true, path, column, val, scoreMode, DEFAULT_BOOST);
    }

    default Children nestedMatch(boolean condition, R path, String column, Object val) {
        return nestedMatch(condition, path, column, val, ScoreMode.Avg, DEFAULT_BOOST);
    }

    default Children nestedMatch(boolean condition, R path, String column, Object val, Float boost) {
        return nestedMatch(condition, path, column, val, ScoreMode.Avg, boost);
    }

    default Children nestedMatch(R path, String column, Object val, Float boost) {
        return nestedMatch(true, path, column, val, ScoreMode.Avg, boost);
    }

    default Children nestedMatch(boolean condition, R path, String column, Object val, ScoreMode scoreMode) {
        return nestedMatch(condition, path, column, val, scoreMode, DEFAULT_BOOST);
    }

    default Children nestedMatch(R path, String column, Object val, ScoreMode scoreMode, Float boost) {
        return nestedMatch(true, path, column, val, scoreMode, boost);
    }

    default Children nestedMatch(boolean condition, R path, String column, Object val, ScoreMode scoreMode, Float boost) {
        return nestedMatch(condition, FieldUtils.getFieldName(path), column, val, scoreMode, boost);
    }

    default Children nestedMatch(String path, String column, Object val) {
        return nestedMatch(true, path, column, val, ScoreMode.Avg, DEFAULT_BOOST);
    }

    default Children nestedMatch(String path, String column, Object val, ScoreMode scoreMode) {
        return nestedMatch(true, path, column, val, scoreMode, DEFAULT_BOOST);
    }

    default Children nestedMatch(boolean condition, String path, String column, Object val) {
        return nestedMatch(condition, path, column, val, ScoreMode.Avg, DEFAULT_BOOST);
    }

    default Children nestedMatch(boolean condition, String path, String column, Object val, Float boost) {
        return nestedMatch(condition, path, column, val, ScoreMode.Avg, boost);
    }

    default Children nestedMatch(String path, String column, Object val, Float boost) {
        return nestedMatch(true, path, column, val, ScoreMode.Avg, boost);
    }

    default Children nestedMatch(boolean condition, String path, String column, Object val, ScoreMode scoreMode) {
        return nestedMatch(condition, path, column, val, scoreMode, DEFAULT_BOOST);
    }

    default Children nestedMatch(String path, String column, Object val, ScoreMode scoreMode, Float boost) {
        return nestedMatch(true, path, column, val, scoreMode, boost);
    }

    /**
     * 嵌套查询 嵌套层级大于1级时适用
     *
     * @param condition 条件
     * @param path      路径
     * @param column    列名
     * @param val       值
     * @param scoreMode 得分模式
     * @param boost     权重
     * @return 泛型
     */
    Children nestedMatch(boolean condition, String path, String column, Object val, ScoreMode scoreMode, Float boost);


    default Children hasChild(String type, String column, Object val) {
        return hasChild(true, type, column, val, ScoreMode.Avg, DEFAULT_BOOST);
    }

    default Children hasChild(String type, String column, Object val, ScoreMode scoreMode) {
        return hasChild(true, type, column, val, scoreMode, DEFAULT_BOOST);
    }

    default Children hasChild(boolean condition, String type, String column, Object val) {
        return hasChild(condition, type, column, val, ScoreMode.Avg, DEFAULT_BOOST);
    }

    default Children hasChild(boolean condition, String type, String column, Object val, Float boost) {
        return hasChild(condition, type, column, val, ScoreMode.Avg, boost);
    }

    default Children hasChild(String type, String column, Object val, Float boost) {
        return hasChild(true, type, column, val, ScoreMode.Avg, boost);
    }

    default Children hasChild(boolean condition, String type, String column, Object val, ScoreMode scoreMode) {
        return hasChild(condition, type, column, val, scoreMode, DEFAULT_BOOST);
    }

    default Children hasChild(String type, String column, Object val, ScoreMode scoreMode, Float boost) {
        return hasChild(true, type, column, val, scoreMode, boost);
    }

    /**
     * 父子类型-根据父查子匹配 返回父文档
     *
     * @param condition 条件
     * @param type      子索引名
     * @param column    列名
     * @param val       值
     * @param scoreMode 得分模式
     * @param boost     权重
     * @return 泛型
     */
    Children hasChild(boolean condition, String type, String column, Object val, ScoreMode scoreMode, Float boost);


    default Children hasParent(String type, String column, Object val) {
        return hasParent(true, type, column, val, true, DEFAULT_BOOST);
    }

    default Children hasParent(String type, String column, Object val, boolean score) {
        return hasParent(true, type, column, val, score, DEFAULT_BOOST);
    }

    default Children hasParent(boolean condition, String type, String column, Object val) {
        return hasParent(condition, type, column, val, true, DEFAULT_BOOST);
    }

    default Children hasParent(boolean condition, String type, String column, Object val, Float boost) {
        return hasParent(condition, type, column, val, true, boost);
    }

    default Children hasParent(String type, String column, Object val, Float boost) {
        return hasParent(true, type, column, val, true, boost);
    }

    default Children hasParent(boolean condition, String type, String column, Object val, boolean score) {
        return hasParent(condition, type, column, val, score, DEFAULT_BOOST);
    }

    default Children hasParent(String type, String column, Object val, boolean score, Float boost) {
        return hasParent(true, type, column, val, score, boost);
    }

    /**
     * 父子类型-根据子查父匹配 返回子文档
     *
     * @param condition 条件
     * @param type      父索引名
     * @param column    列名
     * @param val       值
     * @param score     是否计算评分
     * @param boost     权重
     * @return 泛型
     */
    Children hasParent(boolean condition, String type, String column, Object val, boolean score, Float boost);


    default Children parentId(Object parentId, String type) {
        return parentId(true, parentId, type, DEFAULT_BOOST);
    }

    default Children parentId(boolean condition, Object parentId, String type) {
        return parentId(condition, parentId, type, DEFAULT_BOOST);
    }

    default Children parentId(Object parentId, String type, Float boost) {
        return parentId(true, parentId, type, boost);
    }

    /**
     * 父子类型-根据父id查询 返回父id为指定父id的所有子文档
     *
     * @param condition 条件
     * @param parentId  父id
     * @param type      父索引名
     * @param boost     权重
     * @return 泛型
     */
    Children parentId(boolean condition, Object parentId, String type, Float boost);

    default Children matchPhrase(R column, Object val) {
        return matchPhrase(true, column, val, DEFAULT_BOOST);
    }

    default Children matchPhrase(boolean condition, R column, Object val) {
        return matchPhrase(condition, column, val, DEFAULT_BOOST);
    }

    default Children matchPhrase(R column, Object val, Float boost) {
        return matchPhrase(true, column, val, boost);
    }

    default Children matchPhrase(String column, Object val) {
        return matchPhrase(true, column, val, DEFAULT_BOOST);
    }

    default Children matchPhrase(boolean condition, String column, Object val) {
        return matchPhrase(condition, column, val, DEFAULT_BOOST);
    }

    default Children matchPhrase(String column, Object val, Float boost) {
        return matchPhrase(true, column, val, boost);
    }

    default Children matchPhrase(boolean condition, R column, Object val, Float boost) {
        return matchPhrase(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children matchPhrase(boolean condition, String column, Object val, Float boost);


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


    default Children matchPhrasePrefixQuery(String column, Object val) {
        return matchPhrasePrefixQuery(true, column, val, DEFAULT_MAX_EXPANSIONS, DEFAULT_BOOST);
    }

    default Children matchPhrasePrefixQuery(boolean condition, String column, Object val) {
        return matchPhrasePrefixQuery(condition, column, val, DEFAULT_MAX_EXPANSIONS, DEFAULT_BOOST);
    }

    default Children matchPhrasePrefixQuery(String column, Object val, Float boost) {
        return matchPhrasePrefixQuery(true, column, val, DEFAULT_MAX_EXPANSIONS, boost);
    }

    default Children matchPhrasePrefixQuery(String column, Object val, int maxExpansions) {
        return matchPhrasePrefixQuery(true, column, val, maxExpansions, DEFAULT_BOOST);
    }

    default Children matchPhrasePrefixQuery(String column, Object val, int maxExpansions, Float boost) {
        return matchPhrasePrefixQuery(true, column, val, maxExpansions, boost);
    }

    default Children matchPhrasePrefixQuery(boolean condition, R column, Object val, int maxExpansions, Float boost) {
        return matchPhrasePrefixQuery(condition, FieldUtils.getFieldName(column), val, maxExpansions, boost);
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
    Children matchPhrasePrefixQuery(boolean condition, String column, Object val, int maxExpansions, Float boost);


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

    default Children multiMatchQuery(Object val, String... columns) {
        return multiMatchQuery(true, val, columns);
    }

    default Children multiMatchQuery(boolean condition, Object val, String... columns) {
        return multiMatchQuery(condition, val, Operator.OR, DEFAULT_MIN_SHOULD_MATCH, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, Float boost, String... columns) {
        return multiMatchQuery(true, val, Operator.OR, DEFAULT_MIN_SHOULD_MATCH, boost, columns);
    }

    default Children multiMatchQuery(Object val, int minimumShouldMatch, String... columns) {
        return multiMatchQuery(true, val, Operator.OR, minimumShouldMatch, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, int minimumShouldMatch, String... columns) {
        return multiMatchQuery(true, val, operator, minimumShouldMatch, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, String... columns) {
        return multiMatchQuery(true, val, operator, DEFAULT_MIN_SHOULD_MATCH, DEFAULT_BOOST, columns);
    }

    default Children multiMatchQuery(Object val, int minimumShouldMatch, Float boost, String... columns) {
        return multiMatchQuery(true, val, Operator.OR, minimumShouldMatch, boost, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, Float boost, String... columns) {
        return multiMatchQuery(true, val, operator, DEFAULT_MIN_SHOULD_MATCH, boost, columns);
    }

    default Children multiMatchQuery(Object val, Operator operator, int minimumShouldMatch, Float boost, String... columns) {
        return multiMatchQuery(true, val, operator, minimumShouldMatch, boost, columns);
    }

    default Children multiMatchQuery(boolean condition, Object val, Operator operator, int minimumShouldMatch, Float boost, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return multiMatchQuery(condition, val, operator, minimumShouldMatch, boost, fields);
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
    Children multiMatchQuery(boolean condition, Object val, Operator operator, int minimumShouldMatch, Float boost, String... columns);


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

    default Children prefixQuery(String column, String prefix) {
        return prefixQuery(true, column, prefix);
    }

    default Children prefixQuery(boolean condition, String column, String prefix) {
        return prefixQuery(true, column, prefix, DEFAULT_BOOST);
    }

    default Children prefixQuery(String column, String prefix, Float boost) {
        return prefixQuery(true, column, prefix, boost);
    }

    default Children prefixQuery(boolean condition, R column, String prefix, Float boost) {
        return prefixQuery(condition, FieldUtils.getFieldName(column), prefix, boost);
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
    Children prefixQuery(boolean condition, String column, String prefix, Float boost);


    default Children notMatch(R column, Object val) {
        return notMatch(true, column, val);
    }

    default Children notMatch(R column, Object val, Float boost) {
        return notMatch(true, column, val, boost);
    }

    default Children notMatch(boolean condition, R column, Object val) {
        return notMatch(condition, column, val, DEFAULT_BOOST);
    }

    default Children notMatch(String column, Object val) {
        return notMatch(true, column, val);
    }

    default Children notMatch(String column, Object val, Float boost) {
        return notMatch(true, column, val, boost);
    }

    default Children notMatch(boolean condition, String column, Object val) {
        return notMatch(condition, column, val, DEFAULT_BOOST);
    }

    default Children notMatch(boolean condition, R column, Object val, Float boost) {
        return notMatch(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children notMatch(boolean condition, String column, Object val, Float boost);

    default Children gt(R column, Object val) {
        return gt(true, column, val);
    }

    default Children gt(R column, Object val, Float boost) {
        return gt(true, column, val, boost);
    }

    default Children gt(boolean condition, R column, Object val) {
        return gt(condition, column, val, DEFAULT_BOOST);
    }


    default Children gt(String column, Object val) {
        return gt(true, column, val);
    }

    default Children gt(String column, Object val, Float boost) {
        return gt(true, column, val, boost);
    }

    default Children gt(boolean condition, String column, Object val) {
        return gt(condition, column, val, DEFAULT_BOOST);
    }

    default Children gt(boolean condition, R column, Object val, Float boost) {
        return gt(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children gt(boolean condition, String column, Object val, Float boost);

    default Children ge(R column, Object val) {
        return ge(true, column, val);
    }

    default Children ge(R column, Object val, Float boost) {
        return ge(true, column, val, boost);
    }

    default Children ge(boolean condition, R column, Object val) {
        return ge(condition, column, val, DEFAULT_BOOST);
    }

    default Children ge(String column, Object val) {
        return ge(true, column, val);
    }

    default Children ge(String column, Object val, Float boost) {
        return ge(true, column, val, boost);
    }

    default Children ge(boolean condition, String column, Object val) {
        return ge(condition, column, val, DEFAULT_BOOST);
    }

    default Children ge(boolean condition, R column, Object val, Float boost) {
        return ge(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children ge(boolean condition, String column, Object val, Float boost);

    default Children lt(R column, Object val) {
        return lt(true, column, val);
    }

    default Children lt(R column, Object val, Float boost) {
        return lt(true, column, val, boost);
    }

    default Children lt(boolean condition, R column, Object val) {
        return lt(condition, column, val, DEFAULT_BOOST);
    }

    default Children lt(String column, Object val) {
        return lt(true, column, val);
    }

    default Children lt(String column, Object val, Float boost) {
        return lt(true, column, val, boost);
    }

    default Children lt(boolean condition, String column, Object val) {
        return lt(condition, column, val, DEFAULT_BOOST);
    }

    default Children lt(boolean condition, R column, Object val, Float boost) {
        return lt(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children lt(boolean condition, String column, Object val, Float boost);

    default Children le(R column, Object val) {
        return le(true, column, val);
    }

    default Children le(R column, Object val, Float boost) {
        return le(true, column, val, boost);
    }

    default Children le(boolean condition, R column, Object val) {
        return le(condition, column, val, DEFAULT_BOOST);
    }

    default Children le(String column, Object val) {
        return le(true, column, val);
    }

    default Children le(String column, Object val, Float boost) {
        return le(true, column, val, boost);
    }

    default Children le(boolean condition, String column, Object val) {
        return le(condition, column, val, DEFAULT_BOOST);
    }

    default Children le(boolean condition, R column, Object val, Float boost) {
        return le(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children le(boolean condition, String column, Object val, Float boost);


    default Children between(R column, Object val1, Object val2) {
        return between(true, column, val1, val2);
    }

    default Children between(R column, Object val1, Object val2, Float boost) {
        return between(true, column, val1, val2, boost);
    }

    default Children between(boolean condition, R column, Object val1, Object val2) {
        return between(condition, column, val1, val2, DEFAULT_BOOST);
    }

    default Children between(String column, Object val1, Object val2) {
        return between(true, column, val1, val2);
    }

    default Children between(String column, Object val1, Object val2, Float boost) {
        return between(true, column, val1, val2, boost);
    }

    default Children between(boolean condition, String column, Object val1, Object val2) {
        return between(condition, column, val1, val2, DEFAULT_BOOST);
    }

    default Children between(boolean condition, R column, Object val1, Object val2, Float boost) {
        return between(condition, FieldUtils.getFieldName(column), val1, val2, boost);
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
    Children between(boolean condition, String column, Object val1, Object val2, Float boost);


    default Children notBetween(R column, Object val1, Object val2) {
        return notBetween(true, column, val1, val2);
    }

    default Children notBetween(R column, Object val1, Object val2, Float boost) {
        return notBetween(true, column, val1, val2, boost);
    }

    default Children notBetween(boolean condition, R column, Object val1, Object val2) {
        return notBetween(condition, column, val1, val2, DEFAULT_BOOST);
    }

    default Children notBetween(String column, Object val1, Object val2) {
        return notBetween(true, column, val1, val2);
    }

    default Children notBetween(String column, Object val1, Object val2, Float boost) {
        return notBetween(true, column, val1, val2, boost);
    }

    default Children notBetween(boolean condition, String column, Object val1, Object val2) {
        return notBetween(condition, column, val1, val2, DEFAULT_BOOST);
    }

    default Children notBetween(boolean condition, R column, Object val1, Object val2, Float boost) {
        return notBetween(condition, FieldUtils.getFieldName(column), val1, val2, boost);
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
    Children notBetween(boolean condition, String column, Object val1, Object val2, Float boost);


    default Children like(R column, Object val) {
        return like(true, column, val);
    }

    default Children like(R column, Object val, Float boost) {
        return like(true, column, val, boost);
    }

    default Children like(boolean condition, R column, Object val) {
        return like(condition, column, val, DEFAULT_BOOST);
    }

    default Children like(String column, Object val) {
        return like(true, column, val);
    }

    default Children like(String column, Object val, Float boost) {
        return like(true, column, val, boost);
    }

    default Children like(boolean condition, String column, Object val) {
        return like(condition, column, val, DEFAULT_BOOST);
    }

    default Children like(boolean condition, R column, Object val, Float boost) {
        return like(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children like(boolean condition, String column, Object val, Float boost);

    default Children notLike(R column, Object val) {
        return notLike(true, column, val);
    }

    default Children notLike(R column, Object val, Float boost) {
        return notLike(true, column, val, boost);
    }

    default Children notLike(boolean condition, R column, Object val) {
        return notLike(condition, column, val, DEFAULT_BOOST);
    }

    default Children notLike(String column, Object val) {
        return notLike(true, column, val);
    }

    default Children notLike(String column, Object val, Float boost) {
        return notLike(true, column, val, boost);
    }

    default Children notLike(boolean condition, String column, Object val) {
        return notLike(condition, column, val, DEFAULT_BOOST);
    }

    default Children notLike(boolean condition, R column, Object val, Float boost) {
        return notLike(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children notLike(boolean condition, String column, Object val, Float boost);

    default Children likeLeft(R column, Object val) {
        return likeLeft(true, column, val, DEFAULT_BOOST);
    }

    default Children likeLeft(R column, Object val, Float boost) {
        return likeLeft(true, column, val, boost);
    }

    default Children likeLeft(String column, Object val) {
        return likeLeft(true, column, val, DEFAULT_BOOST);
    }

    default Children likeLeft(String column, Object val, Float boost) {
        return likeLeft(true, column, val, boost);
    }

    default Children likeLeft(boolean condition, R column, Object val, Float boost) {
        return likeLeft(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children likeLeft(boolean condition, String column, Object val, Float boost);


    default Children likeRight(R column, Object val) {
        return likeRight(true, column, val, DEFAULT_BOOST);
    }

    default Children likeRight(R column, Object val, Float boost) {
        return likeRight(true, column, val, boost);
    }

    default Children likeRight(String column, Object val) {
        return likeRight(true, column, val, DEFAULT_BOOST);
    }

    default Children likeRight(String column, Object val, Float boost) {
        return likeRight(true, column, val, boost);
    }

    default Children likeRight(boolean condition, R column, Object val, Float boost) {
        return likeRight(condition, FieldUtils.getFieldName(column), val, boost);
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
    Children likeRight(boolean condition, String column, Object val, Float boost);
}
