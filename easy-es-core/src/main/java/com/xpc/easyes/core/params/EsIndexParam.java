package com.xpc.easyes.core.params;

import lombok.Data;

/**
 * 索引相关参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class EsIndexParam {
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 字段类型
     */
    private String fieldType;
}
