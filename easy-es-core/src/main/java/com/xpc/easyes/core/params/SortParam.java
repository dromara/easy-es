package com.xpc.easyes.core.params;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 排序参数
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 排序参数
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@AllArgsConstructor
public class SortParam {
    /**
     * 是否升序排列
     */
    private Boolean isAsc;
    /**
     * 排序字段
     */
    private List<String> fields;
}
