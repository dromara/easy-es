package com.xpc.easyes.core.conditions.interfaces;

import java.io.Serializable;

/**
 * 连接相关
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 连接关系处理都在此封装
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Join<Children> extends Serializable {
    /**
     * ignore
     *
     * @return
     */
    default Children or() {
        return or(true);
    }

    /**
     * 拼接 OR
     *
     * @param condition 执行条件
     * @return children
     */
    Children or(boolean condition);

}
