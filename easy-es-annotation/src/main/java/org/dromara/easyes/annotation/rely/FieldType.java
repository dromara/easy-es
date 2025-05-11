package org.dromara.easyes.annotation.rely;

import java.util.Arrays;

/**
 * Es支持的数据类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum FieldType {
    /**
     * none Required inside the framework, do not use 框架内部需要,切勿使用,若不慎使用则会被当做keyword_text类型
     */
    NONE("none"),
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
    BINARY("binary"),
    KEYWORD("keyword"),
    TEXT("text"),
    /**
     * This type can not be used for @InnerIndexField
     */
    KEYWORD_TEXT("keyword&text"),
    WILDCARD("wildcard"),
    OBJECT("object"),
    NESTED("nested"),
    JOIN("join"),
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
    PERCOLATOR("percolator"),
    DENSE_VECTOR("dense_vector");

    private String type;

    FieldType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * 根据类型字符串获取对应枚举
     *
     * @param type 类型字符串
     * @return 对应枚举
     */
    public static FieldType getByType(String type) {
        return Arrays.stream(FieldType.values())
                .filter(v -> v.getType().equals(type))
                .findFirst()
                .orElse(FieldType.KEYWORD_TEXT);
    }
}
