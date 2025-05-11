package org.dromara.easyes.core.biz;

import co.elastic.clients.elasticsearch._types.*;
import lombok.Builder;
import lombok.Data;
import org.dromara.easyes.common.enums.OrderTypeEnum;

import java.util.List;

/**
 * 排序基本参数
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
@Builder
public class BaseSortParam {
    /**
     * 排序规则
     */
    private SortOrder sortOrder;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 用户自定义的原生语法排序器
     */
    private SortOptions sortBuilder;

    /**
     * 排序类型
     */
    private OrderTypeEnum orderTypeEnum;

    /**
     * 计算方式 ARC PLANE 默认PLANE
     */
    private GeoDistanceType geoDistanceType;
    /**
     * 距离单位 默认为km
     */
    private DistanceUnit unit;
    /**
     * 排序坐标点
     */
    private List<GeoLocation> geoPoints;
}
