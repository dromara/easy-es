package org.dromara.easyes.common.enums;

/**
 * 查询参数Nested类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum NestedEnum {
    /**
     * 与条件,相当于mysql中的and，必须满足且返回得分
     */
    MUST,
    /**
     * 取反的与条件，必须不满足
     */
    MUST_NOT,
    /**
     * 与条件必须满足，但不返回得分，效率更高
     */
    FILTER,
    /**
     * 或条件，相当于mysql中的or
     */
    SHOULD;
}
