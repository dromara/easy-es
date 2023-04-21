package org.dromara.easyes.annotation.rely;

/**
 * 高亮类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/

public enum HighLightTypeEnum {
    /**
     * unified（通用高亮策略,缺省配置时,默认采用此策略）
     * 其使用的是Lucene的Unified Highlighter。此高亮策略将文本分解成句子，并使用BM25算法对单个句子进行评分，支持精确的短语和多术语(模糊、前缀、正则表达式)突出显示。这个是默认的高亮策略。
     */
    UNIFIED("unified"),
    /**
     * plain （普通高亮策略）
     * 其使用的是Lucene的standard Lucene highlighter。
     * 它试图在理解词的重要性和短语查询中的任何词定位标准方面反映查询匹配逻辑。
     * 此高亮策略是和在单个字段中突出显示简单的查询匹配。如果想用复杂的查询在很多文档中突出显示很多字段，还是使用unified
     */
    PLAIN("plain"),
    /**
     * Fast vector highlighter（快速向量策略）
     * 其使用的是Lucene的Fast Vector highlighter。
     * 注意：使用此策略需要在映射中将对应字段中的属性term_vector设置为with_positions_offsets。
     */
    FVH("fvh");

    /**
     * 聚合类型英文名
     */
    private final String value;

    HighLightTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
