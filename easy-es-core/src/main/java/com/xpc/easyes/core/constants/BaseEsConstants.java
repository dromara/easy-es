package com.xpc.easyes.core.constants;

/**
 * EasyEs的常量
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface BaseEsConstants {
    /**
     * 数字0
     */
    Integer ZERO = 0;
    /**
     * 数字1
     */
    Integer ONE = 1;
    /**
     * 高亮默认前缀标签
     */
    String HIGH_LIGHT_PRE_TAG = "<em>";
    /**
     * 高亮默认后缀标签
     */
    String HIGH_LIGHT_POST_TAG = "</em>";
    /**
     * 默认的当前页码
     */
    Integer PAGE_NUM = 1;
    /**
     * 默认的每页显示条目数
     */
    Integer PAGE_SIZE = 10;
    /**
     * 默认字段boost权重
     */
    Float DEFAULT_BOOST = 1.0F;
    /**
     * 空字符串
     */
    String EMPTY_STR = "";
    /**
     * 冒号
     */
    String COLON = ":";
    /**
     * 分号
     */
    String SEMICOLON = ";";
    /**
     * get 方法前缀
     */
    String GET_FUNC_PREFIX = "get";
    /**
     * set 方法前缀
     */
    String SET_FUNC_PREFIX = "set";
    /**
     * 获取id方法名
     */
    String GET_ID_FUNC = "getId";
    /**
     * 基本数据类型的get方法前缀
     */
    String IS_FUNC_PREFIX = "Is";
    /**
     * 分片数量字段
     */
    String SHARDS_FIELD = "index.number_of_shards";
    /**
     * 副本数量字段
     */
    String REPLICAS_FIELD = "index.number_of_replicas";
    /**
     * 索引特性
     */
    String PROPERTIES = "properties";
    /**
     * 字段类型
     */
    String TYPE = "type";
    /**
     * 通配符
     */
    String WILDCARD_SIGN = "*";
    /**
     * es默认schema
     */
    String DEFAULT_SCHEMA = "http";
    /**
     * 默认返回数
     */
    Integer DEFAULT_SIZE = 10000;
}
