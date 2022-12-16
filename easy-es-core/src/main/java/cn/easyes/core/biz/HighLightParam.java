package cn.easyes.core.biz;

import cn.easyes.annotation.rely.HighLightTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 高亮参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@AllArgsConstructor
public class HighLightParam {
    /**
     * 高亮字段截取长度,默认为100
     */
    private int fragmentSize;
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
    private String highLightField;
    /**
     * 高亮字段类型
     */
    private HighLightTypeEnum highLightType;
}
