package cn.easyes.annotation.rely;

/**
 * Es支持的数据类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum FieldType {
    /**
     * none Required inside the framework, do not use 框架内部需要,切勿使用,若不慎使用则会被当做keyword类型
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
    RANGE("range"),
    BINARY("binary"),
    KEYWORD("keyword"),
    TEXT("text"),
    KEYWORD_TEXT("keyword&text"),
    /**
     * mix
     */
    ARRAY("array"),
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
    PERCOLATOR("percolator");

    private String type;

    FieldType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
