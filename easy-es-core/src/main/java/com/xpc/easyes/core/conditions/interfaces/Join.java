package com.xpc.easyes.core.conditions.interfaces;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;

import java.io.Serializable;

/**
 * 连接相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Join<Children> extends Serializable {
    default Children or() {
        return or(true);
    }

    /**
     * 拼接 OR
     *
     * @param condition 条件
     * @return 泛型
     */
    Children or(boolean condition);
}
