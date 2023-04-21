package org.dromara.easyes.core.conditions.function;

import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.common.settings.Settings;

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
     * @param indexNames 索引名称
     * @return wrapper
     */
    Children indexName(String... indexNames);

    /**
     * 设置最大返回数
     *
     * @param maxResultWindow 最大返回数
     * @return wrapper
     */
    Children maxResultWindow(Integer maxResultWindow);

    /**
     * 设置索引的分片数和副本数
     *
     * @param shards   分片数
     * @param replicas 副本数
     * @return wrapper
     */
    Children settings(Integer shards, Integer replicas);

    /**
     * 用户手动指定的settings
     *
     * @param settings settings
     * @return wrapper
     */
    Children settings(Settings settings);

    /**
     * 用户自行指定mapping
     *
     * @param mapping mapping信息
     * @return wrapper
     */
    Children mapping(Map<String, Object> mapping);

    /**
     * 设置mapping信息
     *
     * @param column    列
     * @param fieldType es中的索引类型
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType) {
        return mapping(column, fieldType, null, null, null, null, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column    列
     * @param fieldType es中的类型
     * @param fieldData 是否支持text字段聚合
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, Boolean fieldData) {
        return mapping(column, fieldType, null, null, null, fieldData, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column 列
     * @param fieldType es中的类型
     * @param boost     权重
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, Float boost) {
        return mapping(column, fieldType, null, null, null, null, boost);
    }

    /**
     * 设置mapping信息
     *
     * @param column    列
     * @param fieldType es中的类型
     * @param fieldData 是否支持text字段聚合
     * @param boost     权重
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, Boolean fieldData, Float boost) {
        return mapping(column, fieldType, null, null, null, fieldData, boost);
    }

    /**
     * 设置mapping信息
     *
     * @param column     列
     * @param fieldType  es中的类型
     * @param dateFormat 日期格式
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, String dateFormat) {
        return mapping(column, fieldType, null, null, dateFormat, null, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列
     * @param fieldType      es中的类型
     * @param analyzer       索引分词器
     * @param searchAnalyzer 查询分词器
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, String analyzer, String searchAnalyzer) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, null, null, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列
     * @param fieldType      es中的类型
     * @param analyzer       索引分词器
     * @param searchAnalyzer 查询分词器
     * @param dateFormat     日期格式
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, dateFormat, null, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列名
     * @param fieldType      es中的类型
     * @param analyzer       索引分词器
     * @param searchAnalyzer 查询分词器
     * @param boost          权重
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, String analyzer, String searchAnalyzer, Float boost) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, null, null, boost);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列
     * @param fieldType      es中的类型
     * @param analyzer       分词器类型
     * @param searchAnalyzer 查询分词器类型
     * @param dateFormat     日期格式
     * @param fieldData      是否支持text字段聚合
     * @param boost          权重值
     * @return wrapper
     */
    default Children mapping(R column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat, Boolean fieldData, Float boost) {
        return mapping(FieldUtils.getFieldName(column), fieldType, analyzer, searchAnalyzer, dateFormat, fieldData, boost);
    }

    /**
     * 设置mapping信息
     *
     * @param column    列名 字符串
     * @param fieldType es中的类型
     * @return wrapper
     */
    default Children mapping(String column, FieldType fieldType) {
        return mapping(column, fieldType, null, null, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column    列名 字符串
     * @param fieldType es中的类型
     * @param fieldData 是否支持text字段聚合
     * @return wrapper
     */
    default Children mapping(String column, FieldType fieldType, Boolean fieldData) {
        return mapping(column, fieldType, null, null, fieldData, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column    列名 字符串
     * @param fieldType es中的类型
     * @param boost     权重
     * @return wrapper
     */
    default Children mapping(String column, FieldType fieldType, Float boost) {
        return mapping(column, fieldType, null, null, null, boost);
    }

    /**
     * 设置mapping信息
     *
     * @param column    列名 字符串
     * @param fieldType es中的类型
     * @param analyzer  索引分词器
     * @return wrapper
     */
    default Children mapping(String column, FieldType fieldType, String analyzer) {
        return mapping(column, fieldType, analyzer, null, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列名 字符串
     * @param fieldType      es中的类型
     * @param analyzer       索引分词器
     * @param searchAnalyzer 查询分词器
     * @return wrapper
     */
    default Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列名 字符串
     * @param fieldType      es中的类型
     * @param analyzer       索引分词器
     * @param searchAnalyzer 查询分词器
     * @param fieldData      是否支持text字段聚合
     * @return wrapper
     */
    default Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, Boolean fieldData) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, fieldData, null);
    }

    /**
     * 设置mapping信息
     *
     * @param column         列名 字符串
     * @param fieldType      es中的类型
     * @param analyzer       索引分词器类型
     * @param searchAnalyzer 查询分词器类型
     * @param fieldData      是否支持text字段聚合
     * @param boost          权重
     * @return wrapper
     */
    default Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, Boolean fieldData, Float boost) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, null, fieldData, boost);
    }


    /**
     * 设置mapping信息
     *
     * @param column         列名 字符串
     * @param fieldType      es中的类型
     * @param analyzer       索引分词器类型
     * @param searchAnalyzer 查询分词器类型
     * @param dateFormat     日期格式
     * @param fieldData      是否支持text字段聚合
     * @param boost          字段权重值
     * @return wrapper
     */
    Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat, Boolean fieldData, Float boost);

    /**
     * 设置创建别名信息
     *
     * @param aliasName 别名
     * @return wrapper
     */
    Children createAlias(String aliasName);

    /**
     * 设置父子类型信息
     *
     * @param column     列
     * @param parentName 父名称
     * @param childName  子名称
     * @return wrapper
     */
    default Children join(R column, String parentName, String childName) {
        return join(FieldUtils.getFieldName(childName), parentName, childName);
    }

    /**
     * 设置父子类型信息
     *
     * @param column     列名 字符串
     * @param parentName 父名称
     * @param childName  子名称
     * @return wrapper
     */
    Children join(String column, String parentName, String childName);
}
