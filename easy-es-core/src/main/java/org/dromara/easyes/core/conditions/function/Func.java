package org.dromara.easyes.core.conditions.function;

import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.util.NamedValue;
import org.dromara.easyes.core.biz.OrderByParam;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.dromara.easyes.core.toolkit.GeoUtils;

import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.dromara.easyes.common.constants.BaseEsConstants.DEFAULT_BOOST;

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
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
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
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列
     * @param geoDistanceType 距离计算方式
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper`
     */
    default Children orderByDistanceAsc(R column, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, DistanceUnit unit, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, DistanceUnit unit, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列
     * @param geoDistanceType 距离计算方式
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, geoDistanceType, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(R column, DistanceUnit unit, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, FieldUtils.getFieldName(column), unit, geoDistanceType, geoPoints);
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
        return orderByDistanceAsc(true, column, DistanceUnit.Kilometers, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
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
        return orderByDistanceAsc(true, column, unit, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param geoDistanceType 距离计算方式
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceAsc(true, column, DistanceUnit.Kilometers, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, DistanceUnit unit, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceAsc(true, column, unit, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, column, DistanceUnit.Kilometers, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, DistanceUnit unit, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, column, unit, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param geoDistanceType 距离计算方式
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, column, DistanceUnit.Kilometers, geoDistanceType, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceAsc(String column, DistanceUnit unit, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceAsc(true, column, unit, geoDistanceType, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param condition       执行条件
     * @param column          列名 字符串
     * @param unit            距离单位 重载方法默认为km
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    Children orderByDistanceAsc(boolean condition, String column, DistanceUnit unit, GeoDistanceType geoDistanceType, GeoLocation... geoPoints);

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column 列
     * @param lat    纬度
     * @param lon    经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, double lat, double lon) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
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
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, DistanceUnit unit, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, DistanceUnit unit, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), DistanceUnit.Kilometers, geoDistanceType, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(R column, DistanceUnit unit, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, FieldUtils.getFieldName(column), unit, geoDistanceType, geoPoints);
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
        return orderByDistanceDesc(true, column, DistanceUnit.Kilometers, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
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
        return orderByDistanceDesc(true, column, unit, GeoDistanceType.Plane, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceDesc(true, column, DistanceUnit.Kilometers, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param lat             纬度
     * @param lon             经度
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, DistanceUnit unit, GeoDistanceType geoDistanceType, double lat, double lon) {
        return orderByDistanceDesc(true, column, unit, geoDistanceType, GeoUtils.create(lat, lon));
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, column, DistanceUnit.Kilometers, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column    列名 字符串
     * @param unit      距离单位
     * @param geoPoints 多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, DistanceUnit unit, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, column, unit, GeoDistanceType.Plane, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, column, DistanceUnit.Kilometers, geoDistanceType, geoPoints);
    }

    /**
     * 地理位置坐标点由近及远排序
     *
     * @param column          列名 字符串
     * @param unit            距离单位
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    default Children orderByDistanceDesc(String column, DistanceUnit unit, GeoDistanceType geoDistanceType, GeoLocation... geoPoints) {
        return orderByDistanceDesc(true, column, unit, geoDistanceType, geoPoints);
    }

    /**
     * 地理位置坐标点由远及近排序
     *
     * @param condition       条件
     * @param column          列名 字符串
     * @param unit            距离单位 重载方法默认为km
     * @param geoDistanceType 距离计算方式,重载方法默认为GeoDistanceType.Plane
     * @param geoPoints       多边形坐标点数组
     * @return wrapper
     */
    Children orderByDistanceDesc(boolean condition, String column, DistanceUnit unit, GeoDistanceType geoDistanceType, GeoLocation... geoPoints);

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
    default Children sort(SortOptions sortBuilder) {
        return sort(true, sortBuilder);
    }

    /**
     * 用户自定义原生排序器
     *
     * @param condition   执行条件
     * @param sortBuilder 原生排序器
     * @return wrapper
     */
    default Children sort(boolean condition, SortOptions sortBuilder) {
        return sort(condition, Collections.singletonList(sortBuilder));
    }

    /**
     * 用户自定义原生排序器
     *
     * @param condition    执行条件
     * @param sortBuilders 原生排序器列表
     * @return wrapper
     */
    Children sort(boolean condition, List<SortOptions> sortBuilders);

    /**
     * 根据得分_score排序 默认为降序 得分高得在前
     *
     * @return wrapper
     */
    default Children sortByScore() {
        return sortByScore(true, SortOrder.Desc);
    }

    /**
     * 根据得分_score排序 默认为降序 得分高得在前
     *
     * @param condition 执行条件
     * @return wrapper
     */
    default Children sortByScore(boolean condition) {
        return sortByScore(condition, SortOrder.Desc);
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
     * 用户自定义SearchRequest.Builder 用于混合查询
     *
     * @param searchBuilder 用户自定义的SearchSourceBuilder
     * @return wrapper
     */
    default Children setSearchBuilder(SearchRequest.Builder searchBuilder) {
        return setSearchBuilder(true, searchBuilder);
    }

    /**
     * 用户自定义SearchRequest.Builder 用于混合查询
     *
     * @param condition     执行条件
     * @param searchBuilder 用户自定义的SearchSourceBuilder
     * @return wrapper
     */
    Children setSearchBuilder(boolean condition, SearchRequest.Builder searchBuilder);

    /**
     * 混合查询
     *
     * @param query 原生查询条件
     * @return wrapper
     */
    default Children mix(co.elastic.clients.elasticsearch._types.query_dsl.Query query) {
        return mix(true, query);
    }


    /**
     * 混合查询
     *
     * @param condition 执行条件
     * @param query     原生查询条件
     * @return wrapper
     */
    Children mix(boolean condition, co.elastic.clients.elasticsearch._types.query_dsl.Query query);


    /**
     * 聚合桶排序
     *
     * @param bucketOrder 排序规则
     * @return wrapper
     */
    default Children bucketOrder(NamedValue<SortOrder> bucketOrder) {
        return bucketOrder(true, bucketOrder);
    }

    /**
     * 聚合桶排序
     *
     * @param condition   条件
     * @param bucketOrder 桶排序规则
     * @return wrapper
     */
    default Children bucketOrder(boolean condition, NamedValue<SortOrder> bucketOrder) {
        return bucketOrder(condition, Arrays.asList(bucketOrder));
    }

    /**
     * 聚合桶排序
     *
     * @param bucketOrders 排序规则列表
     * @return wrapper
     */
    default Children bucketOrder(List<NamedValue<SortOrder>> bucketOrders) {
        return bucketOrder(true, bucketOrders);
    }

    /**
     * 聚合桶排序
     *
     * @param condition    条件
     * @param bucketOrders 排序规则列表
     * @return wrapper
     */
    Children bucketOrder(boolean condition, List<NamedValue<SortOrder>> bucketOrders);

    default Children knn(R column, float[] queryVec, int k) {
        return knn(true, column, queryVec, k);
    }

    default Children knn(boolean condition, R column, float[] queryVec, int k) {
        return knn(condition, FieldUtils.getFieldName(column), queryVec, k);
    }

    /**
     * knn算法向量查询
     *
     * @param condition 执行条件
     * @param column    字段
     * @param queryVec  查询向量
     * @param k         需要返回的最相似的结果数量
     * @return wrapper
     */
    Children knn(boolean condition, String column, float[] queryVec, int k);

    default Children ann(R column, float[] queryVec, int k) {
        return ann(true, column, queryVec, k, k * 10);
    }

    default Children ann(R column, float[] queryVec, int k, int numCandidates) {
        return ann(true, column, queryVec, k, numCandidates);
    }


    default Children ann(boolean condition, R column, float[] queryVec, int k, int numCandidates) {
        return ann(condition, FieldUtils.getFieldName(column), queryVec, k, numCandidates);
    }


    /**
     * ann算法向量查询
     *
     * @param condition     执行条件
     * @param column        字段
     * @param queryVec      查询向量
     * @param k             需要返回的最相似的结果数量
     * @param numCandidates 候选数量
     * @return wrapper
     */
    Children ann(boolean condition, String column, float[] queryVec, int k, int numCandidates);
}
