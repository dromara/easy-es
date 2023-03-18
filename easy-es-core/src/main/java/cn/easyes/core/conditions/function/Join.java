package cn.easyes.core.conditions.function;

import java.io.Serializable;

/**
 * 连接相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Join<Children> extends Serializable {
    /**
     * 拼接filter
     *
     * @return wrapper
     */
    default Children filter() {
        return filter(true);
    }

    /**
     * 拼接filter
     *
     * @param condition 执行条件
     * @return wrapper
     */
    Children filter(boolean condition);

    /**
     * 拼接or
     *
     * @return wrapper
     */
    default Children or() {
        return or(true);
    }

    /**
     * 拼接 OR
     *
     * @param condition 执行条件
     * @return wrapper
     */
    Children or(boolean condition);

    /**
     * 拼接not
     *
     * @return wrapper
     */
    default Children not() {
        return not(true);
    }

    /**
     * 拼接not
     *
     * @param condition 执行条件
     * @return wrapper
     */
    Children not(boolean condition);
}
