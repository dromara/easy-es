package cn.easyes.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Es的连接类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum EsAttachTypeEnum {
    /**
     * 必须满足,相当于MySQL中的and
     */
    MUST(1),
    /**
     * 必须满足,与must区别是不计算得分,效率更高
     */
    FILTER(2),
    /**
     * 或,相当于MySQL中的or
     */
    SHOULD(3),
    /**
     * 否,相当于MySQL中的不等于
     */
    MUST_NOT(4),
    /**
     * 大于 相当于Mysql中的 大于
     */
    GT(5),
    /**
     * 大于等于 相当于Mysql中的 大于等于
     */
    GE(6),
    /**
     * 小于 相当于Mysql中的 小于
     */
    LT(7),
    /**
     * 小于等于 相当于Mysql中的 小于等于
     */
    LE(8),
    /**
     * 在指定集合内 相当于Mysql中的 in
     */
    IN(9),
    /**
     * 不在指定集合内 相当于Mysql中的 not in
     */
    NOT_IN(10),
    /**
     * 存在,相当于Mysql中的 not null
     */
    EXISTS(11),
    /**
     * 不存在,相当于Mysql中的 is null
     */
    NOT_EXISTS(12),
    /**
     * 此区间范围,相当于Mysql中的 between
     */
    BETWEEN(13),
    /**
     * 在此区间范围,相当于Mysql中的 not between
     */
    NOT_BETWEEN(14),
    /**
     * 左右都模糊,相当于Mysql中的like %xxx%
     */
    LIKE(15),
    /**
     * 左模糊,相当于Mysql中的like %xxx
     */
    LIKE_LEFT(16),
    /**
     * 右模糊,相当于Mysql中的like xxx%
     */
    LIKE_RIGHT(17),
    /**
     * 多字段 multiMatchQuery
     */
    MUST_MULTI_FIELDS(18);
    /**
     * 类型
     */
    @Getter
    private Integer type;

}
