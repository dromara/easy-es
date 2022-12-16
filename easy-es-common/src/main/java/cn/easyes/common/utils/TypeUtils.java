package cn.easyes.common.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 核心 泛型工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class TypeUtils {
    /**
     * 获取接口上的泛型T
     *
     * @param o     类
     * @param index 下标
     * @return 类的类型
     */
    public static Class<?> getInterfaceT(Class o, int index) {
        Type[] types = o.getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[index];
        Type type = parameterizedType.getActualTypeArguments()[index];
        return checkType(type, index);

    }

    private static Class<?> checkType(Type type, int index) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type t = pt.getActualTypeArguments()[index];
            return checkType(t, index);
        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType"
                    + ", but <" + type + "> is of type " + className);
        }
    }

}
