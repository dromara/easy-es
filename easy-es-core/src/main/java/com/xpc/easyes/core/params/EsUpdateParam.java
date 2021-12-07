package com.xpc.easyes.core.params;

import lombok.Data;

/**
 * 更新参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class EsUpdateParam {
    /**
     * 字段
     */
    private String field;
    /**
     * 值
     */
    private Object value;
}
