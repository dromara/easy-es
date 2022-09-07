package cn.easyes.core.biz;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.sort.SortOrder;

/**
 * 距离排序参数
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
@Builder
public class DistanceOrderByParam {
    /**
     * 排序字段
     */
    private String fieldName;
    /**
     * 排序规则
     */
    private SortOrder sortOrder;
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
