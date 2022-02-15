package com.xpc.easyes.core.conditions.interfaces;

import org.elasticsearch.common.geo.GeoPoint;

import java.io.Serializable;

import static com.xpc.easyes.core.constants.BaseEsConstants.DEFAULT_BOOST;

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

    /**
     * 矩形
     *
     * @param condition   条件
     * @param column      列
     * @param topLeft     左上点坐标
     * @param bottomRight 右下点坐标
     * @param boost       权重值
     * @return 泛型
     */
    Children geoBoundingBox(boolean condition, R column, GeoPoint topLeft, GeoPoint bottomRight, Float boost);

    default Children geoBoundingBox(R column, String topLeft, String bottomRight) {
        return geoBoundingBox(true, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(boolean condition, R column, String topLeft, String bottomRight) {
        return geoBoundingBox(condition, column, topLeft, bottomRight, DEFAULT_BOOST);
    }

    default Children geoBoundingBox(R column, String topLeft, String bottomRight, Float boost) {
        return geoBoundingBox(true, column, topLeft, bottomRight, boost);
    }

    /**
     * 矩形 字符串经纬度入参(字符串可以是经纬度,以逗号隔开,也可以是哈希值
     *
     * @param condition   条件
     * @param column      列
     * @param topLeft     左上点坐标
     * @param bottomRight 右下点坐标
     * @param boost       权重值
     * @return 泛型
     */
    Children geoBoundingBox(boolean condition, R column, String topLeft, String bottomRight, Float boost);
}
