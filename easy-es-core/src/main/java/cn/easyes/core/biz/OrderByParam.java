package cn.easyes.core.biz;

import lombok.Data;

/**
 * 排序参数
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
