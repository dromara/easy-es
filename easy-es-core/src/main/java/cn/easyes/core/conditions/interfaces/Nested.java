package cn.easyes.core.conditions.interfaces;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 嵌套关系
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Nested<Param, Children> extends Serializable {
    default Children and(Consumer<Param> consumer ) {
        return and(true, consumer);
    }

    /**
     * AND 嵌套
     *
     * @param condition 条件
     * @param consumer      条件函数
     * @return 泛型
     */
    Children and(boolean condition, Consumer<Param> consumer);

    default Children or(Consumer<Param> consumer ) {
        return or(true, consumer);
    }

    /**
     * OR 嵌套
     *
     * @param condition 条件
     * @param consumer      条件函数
     * @return 泛型
     */
    Children or(boolean condition, Consumer<Param> consumer);
}
