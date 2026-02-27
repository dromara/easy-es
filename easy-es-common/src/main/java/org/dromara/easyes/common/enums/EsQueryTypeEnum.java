package org.dromara.easyes.common.enums;

/**
 * 查询类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum EsQueryTypeEnum {
    /**
     * 混合查询，最为特殊的类型 油电混动必备
     */
    MIX,
    /**
     * 精确值匹配 相当于MYSQL 等于
     */
    TERM,
    /**
     * 精确值列表匹配 相当于MYSQL IN
     */
    TERMS,
    /**
     * 模糊匹配 分词 相当于MYSQL LIKE
     */
    MATCH,
    /**
     * 范围查询
     */
    GE,
    GT,
    LE,
    LT,
    BETWEEN,
    /**
     * 存在查询 相当于MYSQL中的 字段 NOT NULL这种查询类型
     */
    EXISTS,
    /**
     * 通配,相当于MYSQL中的LIKE
     */
    WILDCARD,
    /**
     * 分词匹配 需要结果中也包含所有的分词，且顺序一样
     */
    MATCH_PHRASE,
    /**
     * 前缀匹配
     */
    MATCH_PHRASE_PREFIX,
    /**
     * 查询全部 相当于Mysql中的select * 无where条件 谨慎使用
     */
    MATCH_ALL,
    /**
     * 多字段匹配
     */
    MULTI_MATCH,
    /**
     * 所有字段中搜索
     */
    QUERY_STRING,
    /**
     * 前缀查询
     */
    PREFIX,
    /**
     * 地理位置查询
     */
    GEO_BOUNDING_BOX,
    GEO_DISTANCE,
    GEO_POLYGON,
    GEO_SHAPE_ID,
    GEO_SHAPE,
    /**
     * 向量查询
     */
    ANN,
    KNN,
    /**
     * 父子类型查询
     */
    HAS_CHILD,
    HAS_PARENT,
    PARENT_ID,
    /**
     * 与条件,相当于MYSQL中的AND，必须满足且返回得分
     */
    NESTED_AND,
    /**
     * 取反的与条件，必须不满足
     */
    NESTED_NOT,
    /**
     * 与条件必须满足，但不返回得分，效率更高
     */
    NESTED_FILTER,
    /**
     * 或条件，相当于MYSQL中的OR 和MP中的or嵌套用法一致
     */
    NESTED_OR,
    /**
     * 嵌套查询 ES独有 对嵌套类型的查询
     */
    NESTED,
    /**
     * 拼接OR,或条件，和MP中的拼接or用法一致
     */
    OR,
    /**
     * 拼接NOT,非条件 表示必须不满足
     */
    NOT,
    /**
     * 拼接filter,
     */
    FILTER;

}
