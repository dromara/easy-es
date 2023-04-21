package org.dromara.easyes.core.conditions.function;

import org.dromara.easyes.core.biz.OrderByParam;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.*;

import static org.dromara.easyes.common.constants.BaseEsConstants.DEFAULT_BOOST;
import static java.util.stream.Collectors.toList;

/**
 * 高阶语法相关
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@SuppressWarnings("unchecked")
public interface Func<Children, R> extends Serializable {
    /**
     * 升序排列
     *
     * @param column 列
     * @return wrapper
     */
    default Children orderByAsc(R column) {
        return orderByAsc(true, column);
    }

    /**
     * 升序排列
     *
     * @param columns 列
     * @return wrapper
     */
    default Children orderByAsc(R... columns) {
        return orderByAsc(true, columns);
    }

    /**
     * 升序排列
     *
     * @param condition 执行条件
     * @param columns   列
     * @return wrapper
     */
    default Children orderByAsc(boolean condition, R... columns) {
        return orderBy(condition, true, columns);
    }

    /**
     * 降序排列
     *
     * @param column 列
     * @return wrapper
     */
    default Children orderByDesc(R column) {
        return orderByDesc(true, column);
    }

    /**
     * 降序排列
     *
     * @param columns 列
     * @return wrapper
     */
    default Children orderByDesc(R... columns) {
        return orderByDesc(true, columns);
    }

    /**
     * 降序排列
     *
     * @param condition 执行条件
     * @param columns   列
     * @return wrapper
     */
    default Children orderByDesc(boolean condition, R... columns) {
        return orderBy(condition, false, columns);
    }

    /**
     * 升序排列
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children orderByAsc(String column) {
        return orderByAsc(true, column);
    }

    /**
     * 升序排列
     *
     * @param columns 列名 字符串
     * @return wrapper
     */
    default Children orderByAsc(String... columns) {
        return orderByAsc(true, columns);
    }

    /**
     * 升序排列
     *
     * @param condition 执行条件
     * @param columns   列名
     * @return wrapper
     */
    default Children orderByAsc(boolean condition, String... columns) {
        return orderBy(condition, true, columns);
    }

    /**
     * 降序排列
     *
     * @param column 列
     * @return wrapper
     */
    default Children orderByDesc(String column) {
        return orderByDesc(true, column);
    }

    /**
     * 降序排列
     *
     * @param columns 列
     * @return wrapper
     */
    default Children orderByDesc(String... columns) {
        return orderByDesc(true, columns);
    }

    /**
     * 降序排列
     *
     * @param condition 执行条件
     * @param columns   列
     * @return wrapper
     */
    default Children orderByDesc(boolean condition, String... columns) {
        return orderBy(condition, false, columns);
    }

    /**
     * 排序
     *
     * @param condition 执行条件
     * @param isAsc     是否升序排列
     * @param columns   列
     * @return wrapper
     */
    default Children orderBy(boolean condition, boolean isAsc, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return orderBy(condition, isAsc, fields);
    }


    /**
     * 排序
     *
     * @param condition 条件
     * @param isAsc     是否升序 是:按照升序排列,否:按照降序排列
     * @param columns   列,支持多列
     * @return wrapper
     */
    Children orderBy(boolean condition, boolean isAsc, String... columns);

    /**
     * 排序
     *
     * @param orderByParam 排序参数 适用于排序字段和规则从前端通过字符串传入的场景
     * @return wrapper
     */
    default Children orderBy(OrderByParam orderByParam) {
        return orderBy(true, orderByParam);
    }

    /**
     * 排序
     *
     * @param condition    执行条件
     * @param orderByParam 排序参数 适用于排序字段和规则从前端通过字符串传入的场景
     * @return wrapper
     */
    default Children orderBy(boolean condition, OrderByParam orderByParam) {
        return orderBy(condition, Collections.singletonList(orderByParam));
    }

    /**
     * 排序
     *
     * @param orderByParams 排序参数 适用于排序字段和规则从前端通过字符串传入的场景
     * @return wrapper
     */
    default Children orderBy(List<OrderByParam> orderByParams) {
        return orderBy(true, orderByParams);
    }

    /**
     * 排序 适用于排序字段和规则从前端通过字符串传入的场景
     *
     * @param condition     条件
     * @param orderByParams 排序字段及规则参数列表
     * @return wrapper
     */
    Children orderBy(boolean condition, List<OrderByParam> orderByParams);

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, double lat, double lon) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列
     * @param unit   距离单位
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper`
     */
    default Children orderByDistanceAsc(R column, DistanceUnit unit, double lat, double lon) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列
     * @param geoDistance 距离计算方式
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper`
     */
    default Children orderByDistanceAsc(R column, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列
     * @param unit        距离单位
     * @param geoDistance 距离计算方式
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, DistanceUnit unit, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, DistanceUnit unit, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列
     * @param geoDistance 距离计算方式
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列
     * @param unit        距离单位
     * @param geoDistance 距离计算方式
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列名 字符串
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, double lat, double lon) {
        return orderByDistanceAsc(true, column, DistanceUnit.KILOMETERS, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列名 字符串
     * @param unit   距离单位
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, DistanceUnit unit, double lat, double lon) {
        return orderByDistanceAsc(true, column, unit, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param geoDistance 距离计算方式
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceAsc(true, column, DistanceUnit.KILOMETERS, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param unit        距离单位
     * @param geoDistance 距离计算方式
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, DistanceUnit unit, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceAsc(true, column, unit, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, column, DistanceUnit.KILOMETERS, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, DistanceUnit unit, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, column, unit, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param geoDistance 距离计算方式
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, column, DistanceUnit.KILOMETERS, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param unit        距离单位
     * @param geoDistance 距离计算方式
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceAsc(true, column, unit, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param condition   执行条件
     * @param column      列名 字符串
     * @param unit        距离单位 重载方法默认为km
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    Children orderByDistanceAsc(boolean condition, String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints);

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, double lat, double lon) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列
     * @param unit   距离单位
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, DistanceUnit unit, double lat, double lon) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名
     * @param unit        距离单位
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, DistanceUnit unit, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, DistanceUnit unit, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.KILOMETERS, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列
     * @param unit        距离单位
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列名
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, double lat, double lon) {
        return orderByDistanceDesc(true, column, DistanceUnit.KILOMETERS, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列名 字符串
     * @param unit   距离单位
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, DistanceUnit unit, double lat, double lon) {
        return orderByDistanceDesc(true, column, unit, GeoDistance.PLANE, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceDesc(true, column, DistanceUnit.KILOMETERS, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param unit        距离单位
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param lat         纬度
     * @param lon         经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, DistanceUnit unit, GeoDistance geoDistance, double lat, double lon) {
        return orderByDistanceDesc(true, column, unit, geoDistance, new GeoPoint(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, column, DistanceUnit.KILOMETERS, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, DistanceUnit unit, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, column, unit, GeoDistance.PLANE, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, column, DistanceUnit.KILOMETERS, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column      列名 字符串
     * @param unit        距离单位
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints) {
        return orderByDistanceDesc(true, column, unit, geoDistance, geoPoints);
    }

    /**
     * 地理位置坐标点由远及近排序
     *
     * @param condition   条件
     * @param column      列名 字符串
     * @param unit        距离单位 重载方法默认为km
     * @param geoDistance 距离计算方式,重载方法默认为GeoDistance.PLANE
     * @param geoPoints   多边形坐标点数组
     * @return wrapper
     */
    Children orderByDistanceDesc(boolean condition, String column, DistanceUnit unit, GeoDistance geoDistance, GeoPoint... geoPoints);

    /**
     * 字段 IN
     *
     * @param column 列
     * @param coll   值
     * @return wrapper
     */
    default Children in(R column, Collection<?> coll) {
        return in(true, column, coll);
    }

    /**
     * 字段 IN
     *
     * @param condition 执行条件
     * @param column    列
     * @param coll      值
     * @return wrapper
     */
    default Children in(boolean condition, R column, Collection<?> coll) {
        return in(condition, column, coll, DEFAULT_BOOST);
    }

    /**
     * 字段 IN
     *
     * @param column 列
     * @param values 值
     * @return wrapper
     */
    default Children in(R column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 字段 IN
     *
     * @param condition 执行条件
     * @param column    列
     * @param values    值
     * @return wrapper
     */
    default Children in(boolean condition, R column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    /**
     * 字段 IN
     *
     * @param column 列名 字符串
     * @param coll   值
     * @return wrapper
     */
    default Children in(String column, Collection<?> coll) {
        return in(true, column, coll);
    }

    /**
     * 字段 IN
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @param coll      值
     * @return wrapper
     */
    default Children in(boolean condition, String column, Collection<?> coll) {
        return in(condition, column, coll, DEFAULT_BOOST);
    }

    /**
     * 字段 IN
     *
     * @param column 列名 字符串
     * @param values 值
     * @return wrapper
     */
    default Children in(String column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 字段 IN
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @param values    值
     * @return wrapper
     */
    default Children in(boolean condition, String column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    /**
     * 字段 IN
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @param coll      值
     * @param boost     权重
     * @return wrapper
     */
    default Children in(boolean condition, R column, Collection<?> coll, Float boost) {
        return in(condition, FieldUtils.getFieldName(column), coll, boost);
    }

    /**
     * 字段 IN
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @param coll      值
     * @param boost     权重
     * @return wrapper
     */
    Children in(boolean condition, String column, Collection<?> coll, Float boost);

    /**
     * 字段 IS NOT NULL 等价于Es中的exists查询 未废弃是为了兼容mysql用法
     *
     * @param column 列
     * @return wrapper
     */
    default Children isNotNull(R column) {
        return isNotNull(true, column);
    }

    /**
     * 字段 IS NOT NULL 等价于Es中的exists查询 未废弃是为了兼容mysql用法
     *
     * @param condition 执行条件
     * @param column    列
     * @return wrapper
     */
    default Children isNotNull(boolean condition, R column) {
        return isNotNull(condition, column, DEFAULT_BOOST);
    }

    /**
     * 字段 IS NOT NULL 等价于Es中的exists查询 未废弃是为了兼容mysql用法
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children isNotNull(String column) {
        return isNotNull(true, column);
    }

    /**
     * 字段 IS NOT NULL 等价于Es中的exists查询 未废弃是为了兼容mysql用法
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @return wrapper
     */
    default Children isNotNull(boolean condition, String column) {
        return isNotNull(condition, column, DEFAULT_BOOST);
    }

    /**
     * 字段 IS NOT NULL 等价于Es中的exists查询 未废弃是为了兼容mysql用法
     *
     * @param condition 执行条件
     * @param column    列
     * @param boost     权重
     * @return wrapper`
     */
    default Children isNotNull(boolean condition, R column, Float boost) {
        return isNotNull(condition, FieldUtils.getFieldName(column), boost);
    }

    /***
     * 字段 IS NOT NULL 等价于Es中的exists查询 未废弃是为了兼容mysql用法
     * @param condition 执行条件
     * @param column 列名 字符串
     * @param boost 权重
     * @return wrapper
     */
    default Children isNotNull(boolean condition, String column, Float boost) {
        return exists(condition, column, boost);
    }

    /**
     * 字段存在 等价于上面的isNotNull
     *
     * @param column 列
     * @return wrapper
     */
    default Children exists(R column) {
        return isNotNull(true, column);
    }

    /**
     * 字段存在 等价于上面的isNotNull
     *
     * @param condition 执行条件
     * @param column    列
     * @return wrapper
     */
    default Children exists(boolean condition, R column) {
        return isNotNull(condition, column, DEFAULT_BOOST);
    }

    /**
     * 字段存在 等价于上面的isNotNull
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children exists(String column) {
        return isNotNull(true, column);
    }

    /**
     * 字段存在 等价于上面的isNotNull
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @return wrapper
     */
    default Children exists(boolean condition, String column) {
        return exists(condition, column, DEFAULT_BOOST);
    }

    /**
     * 字段存在 等价于上面的isNotNull
     *
     * @param condition 执行条件
     * @param column    列
     * @param boost     权重
     * @return wrapper`
     */
    default Children exists(boolean condition, R column, Float boost) {
        return exists(condition, FieldUtils.getFieldName(column), boost);
    }

    /**
     * 字段存在 等价于上面的isNotNull
     *
     * @param condition 执行条件
     * @param column    列名 字符串
     * @param boost     权重
     * @return wrapper
     */
    Children exists(boolean condition, String column, Float boost);

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param column 列
     * @return wrapper
     */
    default Children groupBy(R column) {
        return groupBy(true, true, column);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列
     * @return wrapper
     */
    default Children groupBy(boolean enablePipeline, R column) {
        return groupBy(true, enablePipeline, column);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param columns 列
     * @return wrapper
     */
    default Children groupBy(R... columns) {
        return groupBy(true, true, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children groupBy(boolean enablePipeline, R... columns) {
        return groupBy(true, enablePipeline, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children groupBy(String column) {
        return groupBy(true, true, column);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列名 字符串
     * @return wrapper
     */
    default Children groupBy(boolean enablePipeline, String column) {
        return groupBy(true, enablePipeline, column);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param columns 列名 字符串
     * @return wrapper
     */
    default Children groupBy(String... columns) {
        return groupBy(true, true, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    default Children groupBy(boolean enablePipeline, String... columns) {
        return groupBy(true, enablePipeline, columns);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children groupBy(boolean condition, boolean enablePipeline, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return groupBy(condition, enablePipeline, fields);
    }

    /**
     * 分组：GROUP BY 字段, ...
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    Children groupBy(boolean condition, boolean enablePipeline, String... columns);

    /**
     * 聚合,等价于GroupBy
     *
     * @param column 列
     * @return wrapper
     */
    default Children termsAggregation(R column) {
        return termsAggregation(true, true, column);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列
     * @return wrapper
     */
    default Children termsAggregation(boolean enablePipeline, R column) {
        return termsAggregation(true, enablePipeline, column);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param columns 列
     * @return wrapper
     */
    default Children termsAggregation(R... columns) {
        return termsAggregation(true, true, columns);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children termsAggregation(boolean enablePipeline, R... columns) {
        return termsAggregation(true, enablePipeline, columns);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children termsAggregation(String column) {
        return termsAggregation(true, true, column);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列名 字符串
     * @return wrapper
     */
    default Children termsAggregation(boolean enablePipeline, String column) {
        return termsAggregation(true, enablePipeline, column);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param columns 列名 字符串
     * @return wrapper
     */
    default Children termsAggregation(String... columns) {
        return termsAggregation(true, true, columns);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    default Children termsAggregation(boolean enablePipeline, String... columns) {
        return termsAggregation(true, enablePipeline, columns);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children termsAggregation(boolean condition, boolean enablePipeline, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return termsAggregation(condition, enablePipeline, fields);
    }

    /**
     * 聚合,等价于GroupBy
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    Children termsAggregation(boolean condition, boolean enablePipeline, String... columns);

    /**
     * 求平均值
     *
     * @param column 列
     * @return wrapper
     */
    default Children avg(R column) {
        return avg(true, true, column);
    }

    /**
     * 求平均值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列
     * @return wrapper
     */
    default Children avg(boolean enablePipeline, R column) {
        return avg(true, enablePipeline, column);
    }

    /**
     * 求平均值
     *
     * @param columns 列
     * @return wrapper
     */
    default Children avg(R... columns) {
        return avg(true, true, columns);
    }

    /**
     * 求平均值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children avg(boolean enablePipeline, R... columns) {
        return avg(true, enablePipeline, columns);
    }

    /**
     * 求平均值
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children avg(String column) {
        return avg(true, true, column);
    }

    /**
     * 求平均值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列名 字符串
     * @return wrapper
     */
    default Children avg(boolean enablePipeline, String column) {
        return avg(true, enablePipeline, column);
    }

    /**
     * 求平均值
     *
     * @param columns 列名 字符串
     * @return wrapper
     */
    default Children avg(String... columns) {
        return avg(true, true, columns);
    }

    /**
     * 求平均值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    default Children avg(boolean enablePipeline, String... columns) {
        return avg(true, enablePipeline, columns);
    }

    /**
     * 求平均值
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children avg(boolean condition, boolean enablePipeline, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return avg(condition, enablePipeline, fields);
    }

    /**
     * 求平均值
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    Children avg(boolean condition, boolean enablePipeline, String... columns);

    /**
     * 求最小值
     *
     * @param column 列
     * @return wrapper
     */
    default Children min(R column) {
        return min(true, true, column);
    }

    /**
     * 求最小值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列
     * @return wrapper
     */
    default Children min(boolean enablePipeline, R column) {
        return min(true, enablePipeline, column);
    }

    /**
     * 求最小值
     *
     * @param columns 列
     * @return wrapper
     */
    default Children min(R... columns) {
        return min(true, true, columns);
    }

    /**
     * 求最小值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children min(boolean enablePipeline, R... columns) {
        return min(true, enablePipeline, columns);
    }

    /**
     * 求最小值
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children min(String column) {
        return min(true, true, column);
    }

    /**
     * 求最小值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列名 字符串
     * @return wrapper
     */
    default Children min(boolean enablePipeline, String column) {
        return min(true, enablePipeline, column);
    }

    /**
     * 求最小值
     *
     * @param columns 列名 字符串
     * @return wrapper
     */
    default Children min(String... columns) {
        return min(true, true, columns);
    }

    /**
     * 求最小值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    default Children min(boolean enablePipeline, String... columns) {
        return min(true, enablePipeline, columns);
    }

    /**
     * 求最小值
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children min(boolean condition, boolean enablePipeline, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return min(condition, enablePipeline, fields);
    }

    /**
     * 求最小值
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    Children min(boolean condition, boolean enablePipeline, String... columns);

    /**
     * 求最大值
     *
     * @param column 列
     * @return wrapper
     */
    default Children max(R column) {
        return max(true, true, column);
    }

    /**
     * 求最大值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列
     * @return wrapper
     */
    default Children max(boolean enablePipeline, R column) {
        return max(true, enablePipeline, column);
    }

    /**
     * 求最大值
     *
     * @param columns 列
     * @return wrapper
     */
    default Children max(R... columns) {
        return max(true, true, columns);
    }

    /**
     * 求最大值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children max(boolean enablePipeline, R... columns) {
        return max(true, enablePipeline, columns);
    }

    /**
     * 求最大值
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children max(String column) {
        return max(true, true, column);
    }

    /**
     * 求最大值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列名 字符串
     * @return wrapper
     */
    default Children max(boolean enablePipeline, String column) {
        return max(true, enablePipeline, column);
    }

    /**
     * 求最大值
     *
     * @param columns 列名 字符串
     * @return wrapper
     */
    default Children max(String... columns) {
        return max(true, true, columns);
    }

    /**
     * 求最大值
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    default Children max(boolean enablePipeline, String... columns) {
        return max(true, enablePipeline, columns);
    }

    /**
     * 求最大值
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children max(boolean condition, boolean enablePipeline, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return max(condition, enablePipeline, fields);
    }

    /**
     * 求最大值
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    Children max(boolean condition, boolean enablePipeline, String... columns);

    /**
     * 求和
     *
     * @param column 列
     * @return wrapper
     */
    default Children sum(R column) {
        return sum(true, true, column);
    }

    /**
     * 求和
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列
     * @return wrapper
     */
    default Children sum(boolean enablePipeline, R column) {
        return sum(true, enablePipeline, column);
    }

    /**
     * 求和
     *
     * @param columns 列
     * @return wrapper
     */
    default Children sum(R... columns) {
        return sum(true, true, columns);
    }

    /**
     * 求和
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children sum(boolean enablePipeline, R... columns) {
        return sum(true, enablePipeline, columns);
    }

    /**
     * 求和
     *
     * @param column 列名 字符串
     * @return wrapper 是否开启管道聚合
     */
    default Children sum(String column) {
        return sum(true, true, column);
    }

    /**
     * 求和
     *
     * @param enablePipeline 是否开启管道聚合
     * @param column         列名 字符串
     * @return wrapper
     */
    default Children sum(boolean enablePipeline, String column) {
        return sum(true, enablePipeline, column);
    }

    /**
     * 求和
     *
     * @param columns 列名 字符串
     * @return wrapper
     */
    default Children sum(String... columns) {
        return sum(true, true, columns);
    }

    /**
     * 求和
     *
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    default Children sum(boolean enablePipeline, String... columns) {
        return sum(true, enablePipeline, columns);
    }

    /**
     * 求和
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列
     * @return wrapper
     */
    default Children sum(boolean condition, boolean enablePipeline, R... columns) {
        String[] fields = Arrays.stream(columns).map(FieldUtils::getFieldName).toArray(String[]::new);
        return sum(condition, enablePipeline, fields);
    }

    /**
     * 求和
     *
     * @param condition      执行条件
     * @param enablePipeline 是否开启管道聚合
     * @param columns        列名 字符串
     * @return wrapper
     */
    Children sum(boolean condition, boolean enablePipeline, String... columns);

    /**
     * 用户自定义原生排序器
     *
     * @param sortBuilder 原生排序器
     * @return wrapper
     */
    default Children sort(SortBuilder<?> sortBuilder) {
        return sort(true, sortBuilder);
    }

    /**
     * 用户自定义原生排序器
     *
     * @param condition   执行条件
     * @param sortBuilder 原生排序器
     * @return wrapper
     */
    default Children sort(boolean condition, SortBuilder<?> sortBuilder) {
        return sort(condition, Collections.singletonList(sortBuilder));
    }

    /**
     * 用户自定义原生排序器
     *
     * @param condition    执行条件
     * @param sortBuilders 原生排序器列表
     * @return wrapper
     */
    Children sort(boolean condition, List<SortBuilder<?>> sortBuilders);

    /**
     * 根据得分_score排序 默认为降序 得分高得在前
     *
     * @return wrapper
     */
    default Children sortByScore() {
        return sortByScore(true, SortOrder.DESC);
    }

    /**
     * 根据得分_score排序 默认为降序 得分高得在前
     *
     * @param condition 执行条件
     * @return wrapper
     */
    default Children sortByScore(boolean condition) {
        return sortByScore(condition, SortOrder.DESC);
    }

    /**
     * 根据得分_score排序
     *
     * @param sortOrder 升序/降序
     * @return wrapper
     */
    default Children sortByScore(SortOrder sortOrder) {
        return sortByScore(true, sortOrder);
    }

    /**
     * 根据得分_score排序
     *
     * @param condition 执行条件
     * @param sortOrder 升序/降序
     * @return wrapper
     */
    Children sortByScore(boolean condition, SortOrder sortOrder);

    /**
     * 单字段去重
     *
     * @param column 列
     * @return wrapper
     */
    default Children distinct(R column) {
        return distinct(true, column);
    }

    /**
     * 单字段去重
     *
     * @param column 列名 字符串
     * @return wrapper
     */
    default Children distinct(String column) {
        return distinct(true, column);
    }

    /**
     * 单字段去重
     *
     * @param condition 执行条件
     * @param column    列
     * @return wrapper
     */
    default Children distinct(boolean condition, R column) {
        return distinct(condition, FieldUtils.getFieldName(column));
    }

    /**
     * 单字段去重
     *
     * @param condition 执行条件
     * @param column    去重字段
     * @return wrapper
     */
    Children distinct(boolean condition, String column);


    /**
     * 从第几条数据开始查询
     *
     * @param from 起始
     * @return wrapper
     */
    Children from(Integer from);

    /**
     * 总共查询多少条数据
     *
     * @param size 查询多少条
     * @return wrapper
     */
    Children size(Integer size);

    /**
     * 兼容MySQL语法 作用同size
     *
     * @param n 查询条数
     * @return wrapper
     */
    Children limit(Integer n);

    /**
     * 兼容MySQL语法 作用同from+size
     *
     * @param m offset偏移量,从第几条开始取,作用同from
     * @param n 查询条数,作用同size
     * @return wrapper
     */
    Children limit(Integer m, Integer n);

    /**
     * 用户自定义SearchSourceBuilder 用于混合查询
     *
     * @param searchSourceBuilder 用户自定义的SearchSourceBuilder
     * @return wrapper
     */
    default Children setSearchSourceBuilder(SearchSourceBuilder searchSourceBuilder) {
        return setSearchSourceBuilder(true, searchSourceBuilder);
    }

    /**
     * 用户自定义SearchSourceBuilder 用于混合查询
     *
     * @param condition           执行条件
     * @param searchSourceBuilder 用户自定义的SearchSourceBuilder
     * @return wrapper
     */
    Children setSearchSourceBuilder(boolean condition, SearchSourceBuilder searchSourceBuilder);

    /**
     * 混合查询
     *
     * @param queryBuilder 原生查询条件
     * @return wrapper
     */
    default Children mix(QueryBuilder queryBuilder) {
        return mix(true, queryBuilder);
    }


    /**
     * 混合查询
     *
     * @param condition    执行条件
     * @param queryBuilder 原生查询条件
     * @return wrapper
     */
    Children mix(boolean condition, QueryBuilder queryBuilder);
}
