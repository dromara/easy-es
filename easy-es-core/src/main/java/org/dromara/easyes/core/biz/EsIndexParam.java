package org.dromara.easyes.core.biz;

import lombok.Data;

import java.util.List;

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
     * 浮点数字段的缩放因子
     */
    private Integer scalingFactor;
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
     * 向量的维度大小，不能超过2048
     */
    private Integer dims;
    /**
     * 相似度算法 例如 dot_product BM25 等
     */
    private String similarity;
    /**
     * 字段是否忽略大小写，默认不忽略 为true时则忽略大小写
     */
    private boolean ignoreCase;
    /**
     * 字段最大索引长度 默认256
     */
    private Integer ignoreAbove;

    /**
     * 内部字段列表
     */
    private List<InnerFieldParam> innerFieldParamList;

    /**
     * 内部段参数
     */
    @Data
    public static class InnerFieldParam {
        /**
         * 内部字段名称
         */
        private String column;
        /**
         * 内部字段类型
         */
        private String fieldType;
        /**
         * 内部分词器
         */
        private String analyzer;
        /**
         * 内部查询分词器
         */
        private String searchAnalyzer;
        /**
         * 内部字段最大索引长度
         */
        private Integer ignoreAbove;
    }
}
