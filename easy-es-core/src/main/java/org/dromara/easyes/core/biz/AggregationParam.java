package org.dromara.easyes.core.biz;

import org.dromara.easyes.common.enums.AggregationTypeEnum;
import lombok.Data;

/**
 * 聚合参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class AggregationParam {
    /**
     * 是否开启管道聚合
     */
    private boolean enablePipeline;
    /**
     * 返回字段名称
     */
    private String name;
    /**
     * 聚合字段
     */
    private String field;
    /**
     * 聚合类型
     */
    private AggregationTypeEnum aggregationType;
}
