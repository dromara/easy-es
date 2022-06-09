package cn.easyes.core.conditions.interfaces;

import cn.easyes.common.utils.CollectionUtils;
import cn.easyes.common.utils.ExceptionUtils;
import cn.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Geometry;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cn.easyes.common.constants.BaseEsConstants.DEFAULT_BOOST;

/**
 * 地理位置
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Geo<Children, R> extends Serializable {
    default Children geoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(condition, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return geoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    default Children geoBoundingBox(R column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return geoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(boolean condition, R column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return geoBoundingBox(condition, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(R column, String topLeft, String bottomRight, Float boost) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return geoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, boost);
    }

    default Children geoBoundingBox(String column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight) {
        return geoBoundingBox(condition, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return geoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    default Children geoBoundingBox(String column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return geoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(boolean condition, String column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return geoBoundingBox(condition, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(String column, String topLeft, String bottomRight, Float boost) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return geoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, boost);
    }

    default Children geoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return geoBoundingBox(condition, FieldUtils.getFieldName(column), topLeft, bottomRight, boost);
    }

    /**
     * 矩形内范围查询
     *
     * @param condition   条件
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return 泛型
     */
    Children geoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost);


    default Children notInGeoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight) {
        return notInGeoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight) {
        return notInGeoBoundingBox(condition, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return notInGeoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    default Children notInGeoBoundingBox(R column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return notInGeoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(boolean condition, R column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return notInGeoBoundingBox(condition, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(R column, String topLeft, String bottomRight, Float boost) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return notInGeoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, boost);
    }

    default Children notInGeoBoundingBox(String column, GeoPoint topLeft, GeoPoint bottomRight) {
        return notInGeoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight) {
        return notInGeoBoundingBox(condition, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return notInGeoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    default Children notInGeoBoundingBox(String column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return notInGeoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(boolean condition, String column, String topLeft, String bottomRight) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return notInGeoBoundingBox(condition, column, topLeftGeoPoint, bottomRightGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoBoundingBox(String column, String topLeft, String bottomRight, Float boost) {
        GeoPoint topLeftGeoPoint = new GeoPoint(topLeft);
        GeoPoint bottomRightGeoPoint = new GeoPoint(bottomRight);
        return notInGeoBoundingBox(true, column, topLeftGeoPoint, bottomRightGeoPoint, boost);
    }

    default Children notInGeoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost) {
        return notInGeoBoundingBox(condition, FieldUtils.getFieldName(column), topLeft, bottomRight, boost);
    }


    /**
     * 矩形外范围查询
     *
     * @param condition   条件
     * @param column      列
     * @param topLeft     左上点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param bottomRight 右下点坐标 GeoPoint/字符串/哈希/wkt均支持
     * @param boost       权重值
     * @return 泛型
     */
    Children notInGeoBoundingBox(boolean condition, String column, GeoPoint topLeft, GeoPoint bottomRight, Float boost);


    default Children geoDistance(R column, Double distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, DistanceUnit.KILOMETERS, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children geoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children geoDistance(String column, Double distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, DistanceUnit.KILOMETERS, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children geoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(condition, FieldUtils.getFieldName(column), distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       条件
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return 泛型
     */
    Children geoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost);


    default Children notInGeoDistance(R column, Double distance, GeoPoint centralGeoPoint) {
        return notInGeoDistance(true, column, distance, DistanceUnit.KILOMETERS, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        return notInGeoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return notInGeoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    default Children notInGeoDistance(R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children notInGeoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children notInGeoDistance(String column, Double distance, GeoPoint centralGeoPoint) {
        return notInGeoDistance(true, column, distance, DistanceUnit.KILOMETERS, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint) {
        return notInGeoDistance(true, column, distance, distanceUnit, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return notInGeoDistance(true, column, distance, distanceUnit, centralGeoPoint, boost);
    }

    default Children notInGeoDistance(String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, distanceUnit, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children notInGeoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, distanceUnit, geoPoint, boost);
    }

    default Children notInGeoDistance(boolean condition, R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost) {
        return notInGeoDistance(condition, FieldUtils.getFieldName(column), distance, distanceUnit, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为双精度
     *
     * @param condition       条件
     * @param column          列
     * @param distance        距离
     * @param distanceUnit    距离单位
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重值
     * @return 泛型
     */
    Children notInGeoDistance(boolean condition, String column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint, Float boost);


    default Children geoDistance(R column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(boolean condition, R column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(R column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, centralGeoPoint, boost);
    }

    default Children geoDistance(R column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(boolean condition, R column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(R column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, geoPoint, boost);
    }

    default Children geoDistance(boolean condition, R column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, geoPoint, boost);
    }

    default Children geoDistance(String column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint) {
        return geoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(String column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(true, column, distance, centralGeoPoint, boost);
    }

    default Children geoDistance(String column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(boolean condition, String column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children geoDistance(String column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(true, column, distance, geoPoint, boost);
    }

    default Children geoDistance(boolean condition, String column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return geoDistance(condition, column, distance, geoPoint, boost);
    }

    default Children geoDistance(boolean condition, R column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return geoDistance(condition, FieldUtils.getFieldName(column), distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       条件
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重
     * @return 泛型
     */
    Children geoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint, Float boost);


    default Children notInGeoDistance(R column, String distance, GeoPoint centralGeoPoint) {
        return notInGeoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(boolean condition, R column, String distance, GeoPoint centralGeoPoint) {
        return notInGeoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(R column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return notInGeoDistance(true, column, distance, centralGeoPoint, boost);
    }

    default Children notInGeoDistance(R column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(boolean condition, R column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(R column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, geoPoint, boost);
    }

    default Children notInGeoDistance(boolean condition, R column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, geoPoint, boost);
    }

    default Children notInGeoDistance(String column, String distance, GeoPoint centralGeoPoint) {
        return notInGeoDistance(true, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint) {
        return notInGeoDistance(condition, column, distance, centralGeoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(String column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return notInGeoDistance(true, column, distance, centralGeoPoint, boost);
    }

    default Children notInGeoDistance(String column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(boolean condition, String column, String distance, String centralGeoPoint) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, geoPoint, DEFAULT_BOOST);
    }

    default Children notInGeoDistance(String column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(true, column, distance, geoPoint, boost);
    }

    default Children notInGeoDistance(boolean condition, String column, String distance, String centralGeoPoint, Float boost) {
        GeoPoint geoPoint = new GeoPoint(centralGeoPoint);
        return notInGeoDistance(condition, column, distance, geoPoint, boost);
    }

    default Children notInGeoDistance(boolean condition, R column, String distance, GeoPoint centralGeoPoint, Float boost) {
        return notInGeoDistance(condition, FieldUtils.getFieldName(column), distance, centralGeoPoint, boost);
    }

    /**
     * 距离范围查询 以给定圆心和半径范围查询 距离类型为字符串
     *
     * @param condition       条件
     * @param column          列
     * @param distance        距离
     * @param centralGeoPoint 中心点 GeoPoint/字符串/哈希/wkt均支持
     * @param boost           权重
     * @return 泛型
     */
    Children notInGeoDistance(boolean condition, String column, String distance, GeoPoint centralGeoPoint, Float boost);


    default Children geoPolygon(R column, List<GeoPoint> geoPoints) {
        return geoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygon(boolean condition, R column, List<GeoPoint> geoPoints) {
        return geoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygon(R column, List<GeoPoint> geoPoints, Float boost) {
        return geoPolygon(true, column, geoPoints, boost);
    }

    default Children geoPolygonStr(R column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return geoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygonStr(boolean condition, R column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return geoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygonStr(R column, List<String> strPoints, Float boost) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return geoPolygon(true, column, geoPoints, boost);
    }

    default Children geoPolygon(String column, List<GeoPoint> geoPoints) {
        return geoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygon(boolean condition, String column, List<GeoPoint> geoPoints) {
        return geoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygon(String column, List<GeoPoint> geoPoints, Float boost) {
        return geoPolygon(true, column, geoPoints, boost);
    }

    default Children geoPolygonStr(String column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return geoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygonStr(boolean condition, String column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return geoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children geoPolygonStr(String column, List<String> strPoints, Float boost) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return geoPolygon(true, column, geoPoints, boost);
    }

    default Children geoPolygon(boolean condition, R column, List<GeoPoint> geoPoints, Float boost) {
        return geoPolygon(condition, FieldUtils.getFieldName(column), geoPoints, boost);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 条件
     * @param column    列
     * @param geoPoints 多边形顶点列表 GeoPoint/字符串/哈希/wkt均支持
     * @param boost     权重值
     * @return 泛型
     */
    Children geoPolygon(boolean condition, String column, List<GeoPoint> geoPoints, Float boost);


    default Children notInGeoPolygon(R column, Collection<GeoPoint> geoPoints) {
        return notInGeoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(boolean condition, R column, Collection<GeoPoint> geoPoints) {
        return notInGeoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(R column, Collection<GeoPoint> geoPoints, Float boost) {
        return notInGeoPolygon(true, column, geoPoints, boost);
    }

    default Children notInGeoPolygon(R column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return notInGeoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(boolean condition, R column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return notInGeoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(R column, List<String> strPoints, Float boost) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return notInGeoPolygon(true, column, geoPoints, boost);
    }

    default Children notInGeoPolygon(String column, Collection<GeoPoint> geoPoints) {
        return notInGeoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(boolean condition, String column, Collection<GeoPoint> geoPoints) {
        return notInGeoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(String column, Collection<GeoPoint> geoPoints, Float boost) {
        return notInGeoPolygon(true, column, geoPoints, boost);
    }

    default Children notInGeoPolygon(String column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return notInGeoPolygon(true, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(boolean condition, String column, List<String> strPoints) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return notInGeoPolygon(condition, column, geoPoints, DEFAULT_BOOST);
    }

    default Children notInGeoPolygon(String column, List<String> strPoints, Float boost) {
        if (CollectionUtils.isEmpty(strPoints)) {
            throw ExceptionUtils.eee("polygon point list must not be empty");
        }
        List<GeoPoint> geoPoints = strPoints.stream().map(GeoPoint::new).collect(Collectors.toList());
        return notInGeoPolygon(true, column, geoPoints, boost);
    }

    default Children notInGeoPolygon(boolean condition, R column, Collection<GeoPoint> geoPoints, Float boost) {
        return notInGeoPolygon(condition, FieldUtils.getFieldName(column), geoPoints, boost);
    }

    /**
     * 不规则多边形范围查询
     *
     * @param condition 条件
     * @param column    列
     * @param geoPoints 多边形顶点列表 GeoPoint/字符串/哈希/wkt均支持
     * @param boost     权重值
     * @return 泛型
     */
    Children notInGeoPolygon(boolean condition, String column, Collection<GeoPoint> geoPoints, Float boost);


    default Children geoShape(R column, String indexedShapeId) {
        return geoShape(true, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children geoShape(R column, String indexedShapeId, Float boost) {
        return geoShape(true, column, indexedShapeId, boost);
    }

    default Children geoShape(boolean condition, R column, String indexedShapeId) {
        return geoShape(condition, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children geoShape(String column, String indexedShapeId) {
        return geoShape(true, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children geoShape(String column, String indexedShapeId, Float boost) {
        return geoShape(true, column, indexedShapeId, boost);
    }

    default Children geoShape(boolean condition, String column, String indexedShapeId) {
        return geoShape(condition, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children geoShape(boolean condition, R column, String indexedShapeId, Float boost) {
        return geoShape(condition, FieldUtils.getFieldName(column), indexedShapeId, boost);
    }


    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param condition      条件
     * @param column         列
     * @param indexedShapeId 已被索引的图形id
     * @param boost          权重值
     * @return 泛型
     */
    Children geoShape(boolean condition, String column, String indexedShapeId, Float boost);


    default Children notInGeoShape(R column, String indexedShapeId) {
        return notInGeoShape(true, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children notInGeoShape(R column, String indexedShapeId, Float boost) {
        return notInGeoShape(true, column, indexedShapeId, boost);
    }

    default Children notInGeoShape(boolean condition, R column, String indexedShapeId) {
        return notInGeoShape(condition, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children notInGeoShape(String column, String indexedShapeId) {
        return notInGeoShape(true, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children notInGeoShape(String column, String indexedShapeId, Float boost) {
        return notInGeoShape(true, column, indexedShapeId, boost);
    }

    default Children notInGeoShape(boolean condition, String column, String indexedShapeId) {
        return notInGeoShape(condition, column, indexedShapeId, DEFAULT_BOOST);
    }

    default Children notInGeoShape(boolean condition, R column, String indexedShapeId, Float boost) {
        return notInGeoShape(condition, FieldUtils.getFieldName(column), indexedShapeId, boost);
    }

    /**
     * 图形GeoShape查询 已知被索引的图形id
     *
     * @param condition      条件
     * @param column         列
     * @param indexedShapeId 已被索引的图形id
     * @param boost          权重值
     * @return 泛型
     */
    Children notInGeoShape(boolean condition, String column, String indexedShapeId, Float boost);


    default Children geoShape(R column, Geometry geometry) {
        return geoShape(true, column, geometry, ShapeRelation.WITHIN, DEFAULT_BOOST);
    }

    default Children geoShape(R column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(true, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children geoShape(boolean condition, R column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(condition, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children geoShape(R column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return geoShape(true, column, geometry, shapeRelation, boost);
    }

    default Children geoShape(String column, Geometry geometry) {
        return geoShape(true, column, geometry, ShapeRelation.WITHIN, DEFAULT_BOOST);
    }

    default Children geoShape(String column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(true, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children geoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation) {
        return geoShape(condition, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children geoShape(String column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return geoShape(true, column, geometry, shapeRelation, boost);
    }

    default Children geoShape(boolean condition, R column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return geoShape(condition, FieldUtils.getFieldName(column), geometry, shapeRelation, boost);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param condition     条件
     * @param column        列
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @param boost         权重值
     * @return 泛型
     */
    Children geoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation, Float boost);


    default Children notInGeoShape(R column, Geometry geometry) {
        return notInGeoShape(true, column, geometry, ShapeRelation.WITHIN, DEFAULT_BOOST);
    }

    default Children notInGeoShape(R column, Geometry geometry, ShapeRelation shapeRelation) {
        return notInGeoShape(true, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children notInGeoShape(boolean condition, R column, Geometry geometry, ShapeRelation shapeRelation) {
        return notInGeoShape(condition, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children notInGeoShape(R column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return notInGeoShape(true, column, geometry, shapeRelation, boost);
    }

    default Children notInGeoShape(String column, Geometry geometry) {
        return notInGeoShape(true, column, geometry, ShapeRelation.WITHIN, DEFAULT_BOOST);
    }

    default Children notInGeoShape(String column, Geometry geometry, ShapeRelation shapeRelation) {
        return notInGeoShape(true, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children notInGeoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation) {
        return notInGeoShape(condition, column, geometry, shapeRelation, DEFAULT_BOOST);
    }

    default Children notInGeoShape(String column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return notInGeoShape(true, column, geometry, shapeRelation, boost);
    }

    default Children notInGeoShape(boolean condition, R column, Geometry geometry, ShapeRelation shapeRelation, Float boost) {
        return notInGeoShape(condition, FieldUtils.getFieldName(column), geometry, shapeRelation, boost);
    }

    /**
     * 图形GeoShape查询 用户指定图形(Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     *
     * @param condition     条件
     * @param column        列
     * @param geometry      图形
     * @param shapeRelation 图形关系(可参考ShapeRelation枚举)
     * @param boost         权重值
     * @return 泛型
     */
    Children notInGeoShape(boolean condition, String column, Geometry geometry, ShapeRelation shapeRelation, Float boost);
}

