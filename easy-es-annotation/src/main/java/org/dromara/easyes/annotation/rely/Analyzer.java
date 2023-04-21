package org.dromara.easyes.annotation.rely;

/**
 * 分词器 如果不包含用户所需分词器,可自行指定传入字符串
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Analyzer {
    /**
     * 不指定分词器
     */
    String NONE = "none";

    //-----------------内置分词器，直接可以使用----------------
    /**
     * 标准分词器
     */
    String STANDARD = "standard";

    /**
     * 简单分词器
     */
    String SIMPLE = "simple";

    /**
     * 停用词分词器
     */
    String STOP = "stop";
    /**
     * 空格分词器
     */
    String WHITESPACE = "whitespace";
    /**
     * 关键词分词器
     */
    String KEYWORD = "keyword";
    /**
     * 正则分词器
     */
    String PATTERN = "pattern";
    /**
     * 语言分词器
     * 如：english、french、chinese
     */
    String LANGUAGE = "language";
    /**
     * 雪球分词器
     */
    String SNOWBALL = "snowball";

    //-------------三方分词，需要另外安装配置，否则无法使用-------------
    /**
     * ik智能分词器
     */
    String IK_SMART = "ik_smart";
    /**
     * ik最大拆分分词器
     */
    String IK_MAX_WORD = "ik_max_word";
    /**
     * 拼音分词器
     */
    String PINYIN = "pinyin";
}
