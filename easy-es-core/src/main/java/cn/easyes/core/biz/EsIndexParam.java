package cn.easyes.core.biz;

import lombok.Data;

/**
 * 索引相关参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class EsIndexParam {
    /**
     * 当前嵌套类
     */
    private Class<?> nestedClass;
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 字段类型
     */
    private String fieldType;
    /**
     * 对 text 字段进行聚合处理
     */
    private Boolean fieldData;
    /**
     * 分词器
     */
    private String analyzer;
    /**
     * 索引权重
     */
    private Float boost;
    /**
     * 查询分词器
     */
    private String searchAnalyzer;
    /**
     * 日期格式化 如yyyy-MM-dd HH:mm:ss
     */
    private String dateFormat;
    /**
     * 父名称
     */
    private String parentName;
    /**
     * 子名称
     */
    private String childName;
}
