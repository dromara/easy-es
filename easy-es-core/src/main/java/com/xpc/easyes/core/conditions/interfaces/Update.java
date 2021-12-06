package com.xpc.easyes.core.conditions.interfaces;

import java.io.Serializable;

/**
 * 更新相关
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 更新相关参数均在此封装
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Update<Children, R> extends Serializable {
    /**
     * ignore
     *
     * @param column
     * @param val
     * @return
     */
    default Children set(R column, Object val) {
        return set(true, column, val);
    }

    /**
     * 设置 更新 SQL 的 SET 片段
     *
     * @param condition 是否加入 set
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children set(boolean condition, R column, Object val);
}
