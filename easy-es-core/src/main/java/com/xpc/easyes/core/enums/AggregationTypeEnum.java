package com.xpc.easyes.core.enums;

/**
 * 聚合枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum AggregationTypeEnum {
    /**
     * 求均值
     */
    AVG,
    /**
     * 求最小值
     */
    MIN,
    /**
     * 求最大值
     */
    MAX,
    /**
     * 求和
     */
    SUM,
    /**
     * 按字段分组,相当于mysql group by
     */
    TERMS;
}
