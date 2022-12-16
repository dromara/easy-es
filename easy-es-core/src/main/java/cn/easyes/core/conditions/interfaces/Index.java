package cn.easyes.core.conditions.interfaces;

import cn.easyes.annotation.rely.FieldType;
import cn.easyes.core.toolkit.FieldUtils;
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
     * @return 泛型
     */
    Children indexName(String... indexNames);

    /**
     * 设置最大返回数
     *
     * @param maxResultWindow 最大返回数
     * @return 泛型
     */
    Children maxResultWindow(Integer maxResultWindow);

    /**
     * 设置索引的分片数和副本数
     *
     * @param shards   分片数
     * @param replicas 副本数
     * @return 泛型
     */
    Children settings(Integer shards, Integer replicas);

    /**
     * 用户手动指定的settings
     *
     * @param settings settings
     * @return 泛型
     */
    Children settings(Settings settings);

    /**
     * 用户自行指定mapping
     *
     * @param mapping mapping信息
     * @return 泛型
     */
    Children mapping(Map<String, Object> mapping);


    default Children mapping(R column, FieldType fieldType) {
        return mapping(column, fieldType, null, null, null, null, null);
    }

    default Children mapping(R column, FieldType fieldType, Boolean fieldData) {
        return mapping(column, fieldType, null, null, null, fieldData, null);
    }

    default Children mapping(R column, FieldType fieldType, Float boost) {
        return mapping(column, fieldType, null, null, null, null, boost);
    }

    default Children mapping(R column, FieldType fieldType, Boolean fieldData, Float boost) {
        return mapping(column, fieldType, null, null, null, fieldData, boost);
    }

    default Children mapping(R column, FieldType fieldType, String dateFormat) {
        return mapping(column, fieldType, null, null, dateFormat, null, null);
    }

    default Children mapping(R column, FieldType fieldType, String analyzer, String searchAnalyzer) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, null, null, null);
    }

    default Children mapping(R column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, dateFormat, null, null);
    }

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
     * @return 泛型
     */
    default Children mapping(R column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat, Boolean fieldData, Float boost) {
        return mapping(FieldUtils.getFieldName(column), fieldType, analyzer, searchAnalyzer, dateFormat, fieldData, boost);
    }


    default Children mapping(String column, FieldType fieldType) {
        return mapping(column, fieldType, null, null, null);
    }

    default Children mapping(String column, FieldType fieldType, Boolean fieldData) {
        return mapping(column, fieldType, null, null, fieldData, null);
    }

    default Children mapping(String column, FieldType fieldType, Float boost) {
        return mapping(column, fieldType, null, null, null, boost);
    }


    default Children mapping(String column, FieldType fieldType, String analyzer) {
        return mapping(column, fieldType, analyzer, null, null);
    }

    default Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, null);
    }

    default Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, Boolean fieldData) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, fieldData, null);
    }

    default Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, Boolean fieldData, Float boost) {
        return mapping(column, fieldType, analyzer, searchAnalyzer, null, fieldData, boost);
    }


    /**
     * 设置mapping信息
     *
     * @param column         列名
     * @param fieldType      es中的类型
     * @param analyzer       分词器类型
     * @param searchAnalyzer 查询分词器类型
     * @param dateFormat     日期格式
     * @param fieldData      是否支持text字段聚合
     * @param boost          字段权重值
     * @return 泛型
     */
    Children mapping(String column, FieldType fieldType, String analyzer, String searchAnalyzer, String dateFormat, Boolean fieldData, Float boost);

    /**
     * 设置创建别名信息
     *
     * @param aliasName 别名
     * @return 泛型
     */
    Children createAlias(String aliasName);


    default Children join(R column, String parentName, String childName) {
        return join(FieldUtils.getFieldName(childName), parentName, childName);
    }

    /**
     * 设置父子类型信息
     *
     * @param column     列名
     * @param parentName 父名称
     * @param childName  子名称
     * @return 泛型
     */
    Children join(String column, String parentName, String childName);
}
