package com.xpc.easyes.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Es支持的数据类型枚举
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: Es处理数据类型, 通常在创建索引时需要
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum FieldType {
    /**
     * core
     */
    BYTE("byte"),
    SHORT("short"),
    INTEGER("integer"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    HALF_FLOAT("half_float"),
    SCALED_FLOAT("scaled_float"),
    BOOLEAN("boolean"),
    DATE("date"),
    RANGE("range"),
    BINARY("binary"),
    KEYWORD("keyword"),
    TEXT("text"),
    /**
     * mix
     */
    ARRAY("array"),
    OBJECT("object"),
    NESTED("nested"),
    /**
     * geo
     */
    GEO_POINT("geo_point"),
    GEO_SHAPE("geo_shape"),
    /**
     * special
     */
    IP("ip"),
    COMPLETION("completion"),
    TOKEN("token"),
    ATTACHMENT("attachment"),
    PERCOLATOR("percolator");
    @Getter
    private String type;
}
