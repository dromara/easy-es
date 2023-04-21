package org.dromara.easyes.core.biz;

import lombok.Data;

/**
 * 自定义排序参数 通常由前端传入
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
public class OrderByParam {
    /**
     * 排序字段
     */
    private String order;
    /**
     * 排序规则 ASC:升序 DESC:降序
     */
    private String sort;
}
