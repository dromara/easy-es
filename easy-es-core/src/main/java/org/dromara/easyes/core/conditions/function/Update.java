package org.dromara.easyes.core.conditions.function;

import org.dromara.easyes.core.toolkit.FieldUtils;

import java.io.Serializable;

/**
 * 更新相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Update<Children, R> extends Serializable {
    /**
     * 设置 更新 字段 的 SET 片段
     *
     * @param column 列
     * @param val    值
     * @return wrapper
     */
    default Children set(R column, Object val) {
        return set(true, column, val);
    }

    /**
     * 设置 更新 字段 的 SET 片段
     *
     * @param column 列名 字符串
     * @param val    值
     * @return wrapper
     */
    default Children set(String column, Object val) {
        return set(true, column, val);
    }

    /**
     * 设置 更新 字段 的 SET 片段
     *
     * @param condition 执行条件
     * @param column    列
     * @param val       值
     * @return wrapper
     */
    default Children set(boolean condition, R column, Object val) {
        return set(condition, FieldUtils.getFieldName(column), val);
    }

    /**
     * 设置 更新 字段 的 SET 片段
     *
     * @param condition 条件
     * @param column    列
     * @param val       值
     * @return wrapper
     */
    Children set(boolean condition, String column, Object val);
}
