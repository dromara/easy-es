package com.xpc.easyes.core.conditions.interfaces;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 嵌套关系
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 嵌套关系相关都在此封装
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Nested<Param, Children> extends Serializable {

    /**
     * ignore
     * @param func
     * @return
     */
    default Children and(Function<Param, Param> func) {
        return and(true, func);
    }

    /**
     * AND 嵌套
     * <p>
     * 例: and(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param func      函数
     * @return children
     */
    Children and(boolean condition, Function<Param, Param> func);

    /**
     * ignore
     * @param func
     * @return
     */
    default Children or(Function<Param, Param> func) {
        return or(true, func);
    }

    /**
     * OR 嵌套
     * <p>
     * 例: or(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param func      函数
     * @return children
     */
    Children or(boolean condition, Function<Param, Param> func);
}
