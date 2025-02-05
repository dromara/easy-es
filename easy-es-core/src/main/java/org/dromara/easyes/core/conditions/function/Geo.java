package org.dromara.easyes.core.conditions.function;

import org.dromara.easyes.common.utils.CollectionUtils;
import org.dromara.easyes.common.utils.ExceptionUtils;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Geometry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.DEFAULT_BOOST;

/**
 * 地理位置
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Geo<Children, R> extends Serializable {
    /**
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    /**
     * 矩形内范围查询
     *
     * @param condition   执行条件
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(condition, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    /**
     * 矩形内范围查询
     *
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return wrapper
     */
    default Children geoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return geoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    /**
     * 矩形内范围查询
     *
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(R column, String topLeft, String bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight);
    }

    /**
     * 矩形内范围查询
     *
     * @param condition   执行条件
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(boolean condition, R column, String topLeft, String bottomRight) {
        return geoBoundingBox(condition, FieldUtils.getFieldName(column), topLeft, bottomRight, DEFAULT_BOOST);
    }

    /**
     * 矩形内范围查询
     *
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return wrapper
     */
    default Children geoBoundingBox(R column, String topLeft, String bottomRight, Float boost) {
        return geoBoundingBox(true, FieldUtils.getFieldName(column), topLeft, bottomRight, boost);
    }

    /**
     * 矩形内范围查询
     *
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(String column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    /**
     * 矩形内范围查询
     *
     * @param condition   执行条件
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(condition, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    /**
     * 矩形内范围查询
     *
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return wrapper
     */
    default Children geoBoundingBox(String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return geoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    /**
     * 矩形内范围查询
     *
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(String column, String topLeft, String bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight);
    }

    /**
     * 矩形内范围查询
     *
     * @param condition   执行条件
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoBoundingBox(boolean condition, String column, String topLeft, String bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    /**
     * 矩形内范围查询
     *
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return wrapper
     */
    default Children geoBoundingBox(String column, String topLeft, String bottomRight, Float boost) {
        return geoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    /**
     * 矩形内范围查询
     *
     * @param condition   执行条件
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return wrapper
     */
    default Children geoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return geoBoundingBox(condition, FieldUtils.getFieldName(column), topLeft, bottomRight, boost);
    }

    /**
     * 矩形内范围查询
     *
     * @param condition   执行条件
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return wrapper
     */
    Children geoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost);

    /**
     * 矩形内范围查询
     *
     * @param condition   执行条件
     * @param column      列名 字符串
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return wrapper
     */
    Children geoBoundingBox(boolean condition, String column, String topLeft, String bottomRight, Float boost);

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(R column, Double distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, DistanceUnit.KILOMETERS, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       执行条件
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        return geoDistance(condition, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       执行条件
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        return geoDistance(condition, FieldUtils.getFieldName(column), distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(String column, Double distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, DistanceUnit.KILOMETERS, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       执行条件
     * @param column          列名 字符串
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        return geoDistance(condition, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       执行条件
     * @param column          列名 字符串
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost);

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       执行条件
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(condition, FieldUtils.getFieldName(column), distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       执行条件
     * @param column          列名 字符串
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost);

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(R column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(boolean condition, R column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(R column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(R column, String distance, String centralGeoPoint) {
        return geoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(boolean condition, R column, String distance, String centralGeoPoint) {
        return geoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(R column, String distance, String centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(boolean condition, R column, String distance, String centralGeoPoint, Float boost) {
        return geoDistance(condition, FieldUtils.getFieldName(column), distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(String column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(String column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(String column, String distance, String centralGeoPoint) {
        return geoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @return wrapper
     */
    default Children geoDistance(boolean condition, String column, String distance, String centralGeoPoint) {
        return geoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(String column, String distance, String centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件
     * @param column          列名 字符串
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    Children geoDistance(boolean condition, String column, String distance, String centralGeoPoint, Float boost);

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return wrapper
     */
    default Children geoDistance(boolean condition, R column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(condition, FieldUtils.getFieldName(column), distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       执行条件       执行条件
     * @param column          列名 字符串          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值           权重
     * @return wrapper wrapper
     */
    Children geoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint, Float boost);

    /**
     * 不规则多边形范围查询
     *
     * @param column    列
     * @param geoPoints 多边形顶点列表
     * @return wrapper
     */
    default Children geoPolygon(R column, List<GeoPoint> geoPoints) {
        return geoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 执行条件
     * @param column    列
     * @param geoPoints 多边形顶点列表
     * @return wrapper
     */
    default Children geoPolygon(boolean condition, R column, List<GeoPoint> geoPoints) {
        return geoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param column    列
     * @param geoPoints 多边形顶点列表
     * @param boost     权重值
     * @return wrapper
     */
    default Children geoPolygon(R column, List<GeoPoint> geoPoints, Float boost) {
        return geoPolygon(true, column, geoPoints, boost);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param column    列
     * @param strPoints wkt字符串多边形
     * @return wrapper
     */
    default Children geoPolygonStr(R column, List<String> strPoints) {
        return geoPolygonStr(true, column, strPoints);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 执行条件
     * @param column    列
     * @param strPoints wkt字符串多边形
     * @return wrapper
     */
    default Children geoPolygonStr(boolean condition, R column, List<String> strPoints) {
        return geoPolygonStr(condition, FieldUtils.getFieldName(column), strPoints);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param column    列
     * @param strPoints wkt字符串多边形
     * @param boost     权重值
     * @return wrapper
     */
    default Children geoPolygonStr(R column, List<String> strPoints, Float boost) {
        return geoPolygonStr(FieldUtils.getFieldName(column), strPoints, boost);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param column    列名 字符串
     * @param geoPoints 多边形顶点列表
     * @return wrapper
     */
    default Children geoPolygon(String column, List<GeoPoint> geoPoints) {
        return geoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @param geoPoints 多边形顶点列表
     * @return wrapper
     */
    default Children geoPolygon(boolean condition, String column, List<GeoPoint> geoPoints) {
        return geoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param column    列名 字符串
     * @param geoPoints 多边形顶点列表
     * @param boost     权重值
     * @return wrapper
     */
    default Children geoPolygon(String column, List<GeoPoint> geoPoints, Float boost) {
        return geoPolygon(true, column, geoPoints, boost);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param column    列名 字符串
     * @param strPoints wkt字符串多边形
     * @return wrapper
     */
    default Children geoPolygonStr(String column, List<String> strPoints) {
        return geoPolygonStr(true, column, strPoints);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @param strPoints wkt字符串多边形
     * @return wrapper
     */
    default Children geoPolygonStr(boolean condition, String column, List<String> strPoints) {
        List<GeoPoint> geoPoints = Optional.ofNullable(strPoints).orElseGet(ArrayList::new)
                .stream()
                .map(GeoPoint::new)
                .collect(Collectors.toList());
        return geoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param column    列名 字符串
     * @param strPoints wkt字符串多边形
     * @param boost     权重值
     * @return wrapper
     */
    default Children geoPolygonStr(String column, List<String> strPoints, Float boost) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return geoPolygon(true, column, geoPoints, boost);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 执行条件
     * @param column    列
     * @param geoPoints 多边形顶点列表
     * @param boost     权重值
     * @return wrapper
     */
    default Children geoPolygon(boolean condition, R column, List<GeoPoint> geoPoints, Float boost) {
        return geoPolygon(condition, FieldUtils.getFieldName(column), geoPoints, boost);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 执行条件 执行条件
     * @param column    列名 字符串    列
     * @param geoPoints 多边形顶点列表 多边形顶点列表 GeoPoint/字符串/哈希/wkt均支持
     * @param boost     权重值     权重值
     * @return wrapper wrapper
     */
    Children geoPolygon(boolean condition, String column, List<GeoPoint> geoPoints, Float boost);


    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param column         列
     * @param indexedShapeId 已被索引的图形id
     * @return wrapper
     */
    default Children geoShape(R column, String indexedShapeId) {
        return geoShape(true, column, indexedShapeId, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param column         列
     * @param indexedShapeId 已被索引的图形id
     * @param boost          权重值
     * @return wrapper
     */
    default Children geoShape(R column, String indexedShapeId, Float boost) {
        return geoShape(true, column, indexedShapeId, boost);
    }

    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param condition      执行条件
     * @param column         列
     * @param indexedShapeId 已被索引的图形id
     * @return wrapper
     */
    default Children geoShape(boolean condition, R column, String indexedShapeId) {
        return geoShape(condition, column, indexedShapeId, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param column         列名 字符串
     * @param indexedShapeId 已被索引的图形id
     * @return wrapper
     */
    default Children geoShape(String column, String indexedShapeId) {
        return geoShape(true, column, indexedShapeId, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param column         列名 字符串
     * @param indexedShapeId 已被索引的图形id
     * @param boost          权重值
     * @return wrapper
     */
    default Children geoShape(String column, String indexedShapeId, Float boost) {
        return geoShape(true, column, indexedShapeId, boost);
    }

    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param condition      执行条件
     * @param column         列名 字符串
     * @param indexedShapeId 已被索引的图形id
     * @return wrapper
     */
    default Children geoShape(boolean condition, String column, String indexedShapeId) {
        return geoShape(condition, column, indexedShapeId, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param condition      执行条件
     * @param column         列
     * @param indexedShapeId 已被索引的图形id
     * @param boost          权重值
     * @return wrapper
     */
    default Children geoShape(boolean condition, R column, String indexedShapeId, Float boost) {
        return geoShape(condition, FieldUtils.getFieldName(column), indexedShapeId, boost);
    }


    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param condition      执行条件
     * @param column         列名 字符串
     * @param indexedShapeId 已被索引的图形id
     * @param boost          权重值
     * @return wrapper
     */
    Children geoShape(boolean condition, String column, String indexedShapeId, Float boost);

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param column   列名 字符串
     * @param geometry 图形
     * @return wrapper
     */
    default Children geoShape(R column, Geometry geometry) {
        return geoShape(true, column, geometry, ShapeRelation.WITHIN, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param column        列名 字符串
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @return wrapper
     */
    default Children geoShape(R column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(true, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param condition     执行条件
     * @param column        列名 字符串
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @return wrapper
     */
    default Children geoShape(boolean condition, R column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(condition, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param column        列名 字符串
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @param boost         权重值
     * @return wrapper
     */
    default Children geoShape(R column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return geoShape(true, column, geometry, shapeRelation, boost);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param column   列名 字符串
     * @param geometry 图形
     * @return wrapper
     */
    default Children geoShape(String column, Geometry geometry) {
        return geoShape(true, column, geometry, ShapeRelation.WITHIN, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param column        列名 字符串
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @return wrapper
     */
    default Children geoShape(String column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(true, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param condition     执行条件
     * @param column        列名 字符串
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @return wrapper
     */
    default Children geoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(condition, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param column        列名 字符串
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @param boost         权重值
     * @return wrapper
     */
    default Children geoShape(String column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return geoShape(true, column, geometry, shapeRelation, boost);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param condition     执行条件
     * @param column        列名 字符串
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @param boost         权重值
     * @return wrapper
     */
    default Children geoShape(boolean condition, R column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return geoShape(condition, FieldUtils.getFieldName(column), geometry, shapeRelation, boost);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param condition     执行条件     执行条件
     * @param column        列名 字符串        列
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @param boost         权重值         权重值
     * @return wrapper wrapper
     */
    Children geoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation, Float boost);
}

