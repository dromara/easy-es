package com.xpc.easyes.core.params;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.common.geo.GeoPoint;

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
     * geoBoundingBox 左上点坐标,字符串形式
     */
    private String topLeftStr;
    /**
     * geoBoundingBox 右下点坐标
     */
    private GeoPoint bottomRight;
    /**
     * geoBoundingBox 右下点坐标,字符串形式
     */
    private String bottomRightStr;
    /**
     * 权重值
     */
    private Float boost;
}
