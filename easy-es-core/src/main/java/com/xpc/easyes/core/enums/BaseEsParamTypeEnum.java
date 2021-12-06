package com.xpc.easyes.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 参数类型
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 参数类型 框架内部逻辑需要
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum BaseEsParamTypeEnum {
    /**
     * AND开头左括号 (
     */
    AND_LEFT_BRACKET(1),
    /**
     * AND开头右括号 )
     */
    AND_RIGHT_BRACKET(2),
    /**
     * OR开头左括号 (
     */
    OR_LEFT_BRACKET(3),
    /**
     * OR开头右括号 )
     */
    OR_RIGHT_BRACKET(4),
    /**
     * OR 左右括号都包含的情况 比如:
     * wrapper.eq(User::getName, "张三")
     * .or()
     * .eq(Document::getAge, 18);
     */
    OR_ALL(5);
    /**
     * 类型
     */
    @Getter
    private Integer type;
}
