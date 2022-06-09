package cn.easyes.core.biz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 排序参数
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
