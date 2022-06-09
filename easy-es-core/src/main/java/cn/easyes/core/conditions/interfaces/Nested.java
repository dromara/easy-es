package cn.easyes.core.conditions.interfaces;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 嵌套关系
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Nested<Param, Children> extends Serializable {
    default Children and(Function<Param, Param> func) {
        return and(true, func);
    }

    /**
     * AND 嵌套
     *
     * @param condition 条件
     * @param func      条件函数
     * @return 泛型
     */
    Children and(boolean condition, Function<Param, Param> func);

    default Children or(Function<Param, Param> func) {
        return or(true, func);
    }

    /**
     * OR 嵌套
     *
     * @param condition 条件
     * @param func      条件函数
     * @return 泛型
     */
    Children or(boolean condition, Function<Param, Param> func);
}
