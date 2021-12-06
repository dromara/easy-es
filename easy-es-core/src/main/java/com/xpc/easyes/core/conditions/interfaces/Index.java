package com.xpc.easyes.core.conditions.interfaces;

import com.xpc.easyes.core.enums.FieldType;

import java.io.Serializable;

/**
 * 索引相关
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 索引相关参数都在此封装
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Index<Children, R> extends Serializable {
    /**
     * 设置索引名称
     *
     * @param indexName
     * @return
     */
    Children indexName(String indexName);

    /**
     * 设置索引的分片数和副本数
     *
     * @param shards   分片数
     * @param replicas 副本数
     * @return
     */
    Children settings(Integer shards, Integer replicas);

    /**
     * 设置mapping信息
     *
     * @param column
     * @param fieldType
     * @return
     */
    Children mapping(R column, FieldType fieldType);

    /**
     * 设置创建别名信息
     *
     * @param aliasName
     * @return
     */
    Children createAlias(String aliasName);
}
