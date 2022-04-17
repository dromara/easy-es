package com.xpc.easyes.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public enum JdkDataTypeEnum {
    BYTE("byte"),
    SHORT("short"),
    INT("int"),
    INTEGER("integer"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BIG_DECIMAL("bigdecimal"),
    BOOLEAN("boolean"),
    CHAR("char"),
    STRING("string"),
    DATE("date"),
    LOCAL_DATE("localdate"),
    LOCAL_DATE_TIME("localdatetime"),
    LIST("list");
    @Getter
    private String type;

    public static JdkDataTypeEnum getByType(String typeName) {
        return Arrays.stream(JdkDataTypeEnum.values())
                .filter(v -> Objects.equals(v.type, typeName))
                .findFirst()
                .orElse(STRING);
    }
}
