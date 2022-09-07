package cn.easyes.core.conditions.interfaces;

import cn.easyes.core.biz.EntityFieldInfo;
import cn.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * 查询相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Query<Children, T, R> extends Serializable {

    default Children select(R column) {
        return select(FieldUtils.getFieldNameNotConvertId(column));
    }

    default Children select(R... columns) {
        return select(Arrays.stream(columns).map(FieldUtils::getFieldNameNotConvertId).toArray(String[]::new));
    }


    /**
     * 设置查询字段
     *
     * @param columns 查询列,支持多字段
     * @return 泛型
     */
    Children select(String... columns);

    /**
     * 查询字段
     *
     * @param predicate 预言
     * @return 泛型
     */
    Children select(Predicate<EntityFieldInfo> predicate);

    /**
     * 过滤查询的字段信息(主键除外!)
     *
     * @param entityClass 实体类
     * @param predicate   预言
     * @return 泛型
     */
    Children select(Class<T> entityClass, Predicate<EntityFieldInfo> predicate);

    default Children notSelect(R column) {
        return notSelect(FieldUtils.getFieldNameNotConvertId(column));
    }

    default Children notSelect(R... columns) {
        return notSelect(Arrays.stream(columns).map(FieldUtils::getFieldNameNotConvertId).toArray(String[]::new));
    }

    /**
     * 设置不查询字段
     *
     * @param columns 不查询字段,支持多字段
     * @return 泛型
     */
    Children notSelect(String... columns);

    /**
     * 从第几条数据开始查询
     *
     * @param from 起始
     * @return 泛型
     */
    Children from(Integer from);

    /**
     * 总共查询多少条数据
     *
     * @param size 查询多少条
     * @return 泛型
     */
    Children size(Integer size);

    /**
     * 兼容MySQL语法 作用同size
     *
     * @param n 查询条数
     * @return 泛型
     */
    Children limit(Integer n);

    /**
     * 兼容MySQL语法 作用同from+size
     *
     * @param m offset偏移量,从第几条开始取,作用同from
     * @param n 查询条数,作用同size
     * @return 泛型
     */
    Children limit(Integer m, Integer n);

    default Children index(String indexName) {
        return index(true, indexName);
    }

    /**
     * 设置当前查询的索引名称
     *
     * @param condition 条件
     * @param indexName 索引名
     * @return 泛型
     */
    Children index(boolean condition, String indexName);


    default Children enableMust2Filter(boolean enable) {
        return enableMust2Filter(true, enable);
    }

    /**
     * must 条件转filter 默认不转换
     *
     * @param condition 条件
     * @param enable    是否开启 true开启 false 不开启 默认不开转换
     * @return 泛型
     */
    Children enableMust2Filter(boolean condition, boolean enable);

    default Children setSearchSourceBuilder(SearchSourceBuilder searchSourceBuilder) {
        return setSearchSourceBuilder(true, searchSourceBuilder);
    }

    /**
     * 用户自定义SearchSourceBuilder 用于混合查询
     *
     * @param condition           条件
     * @param searchSourceBuilder 用户自定义的SearchSourceBuilder
     * @return 泛型
     */
    Children setSearchSourceBuilder(boolean condition, SearchSourceBuilder searchSourceBuilder);

}
