package com.xpc.easyes.core.conditions.interfaces;

import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldType;

import java.io.Serializable;
import java.util.Map;

/**
 * 索引相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Index<Children, R> extends Serializable {
    /**
     * 设置索引名称
     *
     * @param indexName 索引名称
     * @return 泛型
     */
    Children indexName(String indexName);

    /**
     * 设置索引的分片数和副本数
     *
     * @param shards   分片数
     * @param replicas 副本数
     * @return 泛型
     */
    Children settings(Integer shards, Integer replicas);

    /**
     * 用户自行指定mapping
     *
     * @param mapping mapping信息
     * @return 泛型
     */
    Children mapping(Map<String, Object> mapping);


    default Children mapping(R column, FieldType fieldType) {
        return mapping(column, fieldType, null, null, null);
    }

    default Children mapping(R column, FieldType fieldType, String dateFormat) {
        return mapping(column, fieldType, null, null, dateFormat);
    }

    default Children mapping(R column, FieldType fieldType, Analyzer analyzer) {
        return mapping(column, fieldType, analyzer, null, null);
    }

    default Children mapping(R column, FieldType fieldType, Analyzer analyzer, String dateFormat) {
        return mapping(column, fieldType, analyzer, null, dateFormat);
    }

    default Children mapping(R colmun, FieldType fieldType, Analyzer analyzer, Analyzer searchAnalyzer) {
        return mapping(colmun, fieldType, analyzer, searchAnalyzer, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列
     * @param fieldType      es中的类型
     * @param analyzer       分词器类型
     * @param searchAnalyzer 查询分词器类型
     * @param dateFormat     日期格式
     * @return 泛型
     */
    Children mapping(R column, FieldType fieldType, Analyzer analyzer, Analyzer searchAnalyzer, String dateFormat);


    default Children mapping(String column, FieldType fieldType) {
        return mapping(column, fieldType, null, null, null);
    }

    default Children mapping(String column, FieldType fieldType, String dateFormat) {
        return mapping(column, fieldType, null, null, dateFormat);
    }

    default Children mapping(String column, FieldType fieldType, Analyzer analyzer) {
        return mapping(column, fieldType, analyzer, null, null);
    }

    default Children mapping(String column, FieldType fieldType, Analyzer analyzer, String dateFormat) {
        return mapping(column, fieldType, analyzer, null, dateFormat);
    }

    default Children mapping(String column, FieldType fieldType, Analyzer analyzer, Analyzer searchAnalyzer) {
        return mapping(column, fieldType, analyzer, null, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列名
     * @param fieldType      es中的类型
     * @param analyzer       分词器类型
     * @param searchAnalyzer 查询分词器类型
     * @param dateFormat     日期格式
     * @return 泛型
     */
    Children mapping(String column, FieldType fieldType, Analyzer analyzer, Analyzer searchAnalyzer, String dateFormat);

    /**
     * 设置创建别名信息
     *
     * @param aliasName 别名
     * @return 泛型
     */
    Children createAlias(String aliasName);
}
