package org.dromara.easyes.core.conditions.function;

import org.dromara.easyes.core.biz.EntityFieldInfo;
import org.dromara.easyes.core.toolkit.FieldUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * 查询相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Query<Children, T, R> extends Serializable {
    /**
     * 设置查询字段
     *
     * @param column 查询列
     * @return wrapper
     */
    default Children select(R column) {
        return select(FieldUtils.getFieldNameNotConvertId(column));
    }

    /**
     * 设置查询字段
     *
     * @param columns 查询列
     * @return wrapper
     */
    default Children select(R... columns) {
        return select(Arrays.stream(columns).map(FieldUtils::getFieldNameNotConvertId).toArray(String[]::new));
    }


    /**
     * 设置查询字段
     *
     * @param columns 查询列,支持多字段
     * @return wrapper
     */
    Children select(String... columns);

    /**
     * 查询字段
     *
     * @param predicate 预言
     * @return wrapper
     */
    Children select(Predicate<EntityFieldInfo> predicate);

    /**
     * 过滤查询的字段信息(主键除外!)
     *
     * @param entityClass 实体类
     * @param predicate   预言
     * @return wrapper
     */
    Children select(Class<T> entityClass, Predicate<EntityFieldInfo> predicate);

    /**
     * 设置不查询字段
     *
     * @param column 不查询列
     * @return wrapper
     */
    default Children notSelect(R column) {
        return notSelect(FieldUtils.getFieldNameNotConvertId(column));
    }

    /**
     * 设置不查询字段
     *
     * @param columns 不查询列
     * @return wrapper
     */
    default Children notSelect(R... columns) {
        return notSelect(Arrays.stream(columns).map(FieldUtils::getFieldNameNotConvertId).toArray(String[]::new));
    }

    /**
     * 设置最小得分 低于此分值的文档将不召回
     *
     * @param score 最小得分
     * @return wrapper
     */
    Children minScore(Double score);

    /**
     * 开启计算得分 默认值为关闭状态
     *
     * @return wrapper
     */
    Children trackScores();

    /**
     * 设置不查询字段
     *
     * @param columns 不查询字段,支持多字段
     * @return wrapper
     */
    Children notSelect(String... columns);

    /**
     * 设置当前操作的索引名称
     *
     * @param indexName 索引名
     * @return wrapper
     */
    default Children index(String indexName) {
        return index(true, indexName);
    }

    /**
     * 设置当前操作的索引名称
     *
     * @param indexNames 索引名
     * @return wrapper
     */
    default Children index(String... indexNames) {
        return index(true, indexNames);
    }

    /**
     * 设置当前操作的索引名称
     *
     * @param condition  条件
     * @param indexNames 索引名
     * @return wrapper
     */
    Children index(boolean condition, String... indexNames);

    /**
     * 设置当前操作的路由
     *
     * @param routing 路由
     * @return wrapper
     */
    default Children routing(String routing) {
        return routing(true, routing);
    }

    /**
     * 设置当前操作的路由
     *
     * @param condition 条件
     * @param routing   路由
     * @return wrapper
     */
    Children routing(boolean condition, String routing);

    /**
     * 设置查询偏好
     *
     * @param preference 偏好
     * @return wrapper
     */
    default Children preference(String preference) {
        return preference(true, preference);
    }

    /**
     * 设置查询偏好
     *
     * @param condition  条件
     * @param preference 偏好
     * @return wrapper
     */
    Children preference(boolean condition, String preference);

}
