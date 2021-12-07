package com.xpc.easyes.core.params;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 高亮参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@AllArgsConstructor
public class HighLightParam {
    /**
     * 前置标签
     */
    private String preTag;
    /**
     * 后置标签
     */
    private String postTag;
    /**
     * 高亮字段列表
     */
    private List<String> fields;
}
