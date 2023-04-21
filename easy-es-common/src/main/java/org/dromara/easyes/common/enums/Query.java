package org.dromara.easyes.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询类型枚举
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum Query {
    /**
     * 不分词匹配,默认为精确匹配,如果需要左模糊,右模糊或者左右都模糊,请在传入的查询字段值中拼接"*" 类似MySQL的like拼接%
     */
    EQ(".keyword"),
    /**
     * 分词匹配
     */
    MATCH("");
    @Getter
    private String text;
}
