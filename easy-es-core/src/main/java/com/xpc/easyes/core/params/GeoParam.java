package com.xpc.easyes.core.params;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Geometry;

import java.util.List;

/**
 * Geo相关参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@Builder
public class GeoParam {
    /**
     * 字段名
     */
    private String field;
    /**
     * geoBoundingBox 左上点坐标
     */
    private GeoPoint topLeft;
    /**
     * geoBoundingBox 右下点坐标
     */
    private GeoPoint bottomRight;
    /**
     * 中心点坐标
     */
    private GeoPoint centralGeoPoint;
    /**
     * 距离 双精度类型
     */
    private Double distance;
    /**
     * 距离 单位
     */
    private DistanceUnit distanceUnit;
    /**
     * 距离 字符串类型
     */
    private String distanceStr;
    /**
     * 不规则坐标点列表
     */
    private List<GeoPoint> geoPoints;
    /**
     * 已被索引形状的索引id
     */
    private String indexedShapeId;
    /**
     * 图形
     */
    private Geometry geometry;
    /**
     * 图形关系
     */
    private ShapeRelation shapeRelation;
    /**
     * 权重值
     */
    private Float boost;
}
