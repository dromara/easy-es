package org.dromara.easyes.annotation.rely;

/**
 * 注解依赖常量
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public interface AnnotationConstants {
    /**
     * 高亮默认前缀标签
     */
    String HIGH_LIGHT_PRE_TAG = "<em>";
    /**
     * 高亮默认后缀标签
     */
    String HIGH_LIGHT_POST_TAG = "</em>";
    /**
     * 高亮截取默认长度
     */
    int DEFAULT_FRAGMENT_SIZE = 100;
    /**
     * 默认分片数
     */
    int DEFAULT_SHARDS = 1;
    /**
     * 默认副本数
     */
    int DEFAULT_REPLICAS = 1;
    /**
     * 默认最大返回数
     */
    int DEFAULT_MAX_RESULT_WINDOW = 10000;
    /**
     * 默认索引别名
     */
    String DEFAULT_ALIAS = "ee_default_alias";
    /**
     * 默认join字段名称
     */
    String DEFAULT_JOIN_FIELD_NAME = "joinField";
}
