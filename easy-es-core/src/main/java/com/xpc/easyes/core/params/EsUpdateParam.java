package com.xpc.easyes.core.params;

import lombok.Data;

/**
 * 更新参数
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 更新参数
 * @Author: xpc
 * @Version: 1.0
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
