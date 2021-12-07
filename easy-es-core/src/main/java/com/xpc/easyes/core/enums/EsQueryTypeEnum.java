package com.xpc.easyes.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum EsQueryTypeEnum {
    /**
     * 精确值匹配 相当于MySQL 等于
     */
    TERM_QUERY(1),
    /**
     * 精确值列表匹配 相当于mysql in
     */
    TERMS_QUERY(2),
    /**
     * 模糊匹配 分词 相当于mysql like
     */
    MATCH_QUERY(3),
    /**
     * 范围查询
     */
    RANGE_QUERY(4),
    /**
     * 区间查询,特殊的RANGE_QUERY 相当于mysql between
     */
    INTERVAL_QUERY(5),
    /**
     * 存在查询 相当于Mysql中的 is null,not null这种查询类型
     */
    EXISTS_QUERY(6),
    /**
     * 聚合查询 相当于mysql中的 group by 当然 不仅限于group by 还新增了 sum,avg,max,min等功能
     */
    AGGREGATION_QUERY(7),
    /**
     * 通配,相当于mysql中的like
     */
    WILDCARD_QUERY(8);

    /**
     * 类型
     */
    @Getter
    private Integer type;
}
