package com.xpc.easyes.core.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页参数 来源:https://github.com/pagehelper/Mybatis-PageHelper
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 保持和github最受欢迎的PageHelper插件一样的参数 确保用户使用及切换零额外成本
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@NoArgsConstructor
public class PageSerializable<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 总记录数
     */
    protected long total;
    /**
     * 结果集
     */
    protected List<T> list;

    public PageSerializable(List<T> list) {
        this.list = list;
        this.total = list.size();
    }

    public static <T> PageSerializable<T> of(List<T> list) {
        return new PageSerializable<T>(list);
    }
}
