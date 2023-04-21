package org.dromara.easyes.core.biz;

import org.dromara.easyes.common.enums.OrderTypeEnum;
import lombok.Builder;
import lombok.Data;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

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
    private SortBuilder<?> sortBuilder;

    /**
     * 排序类型
     */
    private OrderTypeEnum orderTypeEnum;

    /**
     * 计算方式 ARC PLANE 默认PLANE
     */
    private GeoDistance geoDistance;
    /**
     * 距离单位 默认为km
     */
    private DistanceUnit unit;
    /**
     * 排序坐标点
     */
    private GeoPoint[] geoPoints;
}
