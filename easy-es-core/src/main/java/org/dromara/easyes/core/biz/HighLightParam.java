package org.dromara.easyes.core.biz;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.easyes.annotation.rely.HighLightTypeEnum;

/**
 * 高亮参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@Accessors(chain = true)
public class HighLightParam {
    /**
     * 高亮字段截取长度,默认为100
     */
    private Integer fragmentSize;
    /**
     * 搜索返回的高亮片段数量 默认全部返回
     */
    private Integer numberOfFragments;
    /**
     * 前置标签
     */
    private String preTag;
    /**
     * 后置标签
     */
    private String postTag;
    /**
     * 是否需要与查询字段匹配
     */
    protected Boolean requireFieldMatch;
    /**
     * 高亮字段列表
     */
    private String highLightField;
    /**
     * 高亮字段类型
     */
    private HighLightTypeEnum highLightType;
}
