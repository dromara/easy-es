package cn.easyes.core.conditions.interfaces;

import cn.easyes.core.toolkit.FieldUtils;

import java.io.Serializable;

/**
 * 更新相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Update<Children, R> extends Serializable {
    default Children set(R column, Object val) {
        return set(true, column, val);
    }

    default Children set(String column, Object val) {
        return set(true, column, val);
    }

    default Children set(boolean condition, R column, Object val) {
        return set(condition, FieldUtils.getFieldName(column), val);
    }

    /**
     * 设置 更新 SQL 的 SET 片段
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @return 泛型
     */
    Children set(boolean condition, String column, Object val);


    default Children index(String indexName) {
        return index(true, indexName);
    }

    /**
     * 设置当前更新操作作用的索引
     *
     * @param condition 条件
     * @param indexName 索引名
     * @return 泛型
     */
    Children index(boolean condition, String indexName);
}
