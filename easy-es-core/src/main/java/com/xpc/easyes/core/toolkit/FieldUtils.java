package com.xpc.easyes.core.toolkit;

import com.xpc.easyes.core.conditions.interfaces.SFunction;
import com.xpc.easyes.core.constants.BaseEsConstants;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 核心 处理字段名称工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class FieldUtils {
    private FieldUtils() {
    }

    /**
     * 获取字段名称
     *
     * @param func 列函数
     * @param <R>  泛型
     * @return 泛型
     */
    public static <R> String getFieldName(R func) {
        if (!(func instanceof SFunction)) {
            throw new RuntimeException("not support this type of column");
        }

        try {
            // 通过获取对象方法，判断是否存在该方法
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            // 利用jdk的SerializedLambda 解析方法引用
            java.lang.invoke.SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String getter = serializedLambda.getImplMethodName();
            return resolveFieldName(getter);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理获取字段名称
     *
     * @param getMethodName get方法的名字
     * @return 字段名称
     */
    public static String resolveFieldName(String getMethodName) {
        if (getMethodName.startsWith(BaseEsConstants.GET_FUNC_PREFIX)) {
            getMethodName = getMethodName.substring(3);
        } else if (getMethodName.startsWith(BaseEsConstants.IS_FUNC_PREFIX)) {
            getMethodName = getMethodName.substring(2);
        }
        // 小写第一个字母
        return firstToLowerCase(getMethodName);
    }

    /**
     * 获取Get方法名称
     *
     * @param fieldName 字段名称
     * @return Get方法名称
     */
    public static String generateGetFunctionName(String fieldName) {
        return BaseEsConstants.GET_FUNC_PREFIX + firstToUpperCase(fieldName);
    }

    /**
     * 获取Set方法名称
     *
     * @param fieldName 字段名称
     * @return et方法名称
     */
    public static String generateSetFunctionName(String fieldName) {
        return BaseEsConstants.SET_FUNC_PREFIX + firstToUpperCase(fieldName);
    }

    /**
     * 将首字母小写
     *
     * @param param 参数
     * @return 首字母小写后的结果
     */
    private static String firstToLowerCase(String param) {
        if (Objects.isNull(param) || "".equals(param)) {
            return "";
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    /**
     * 将首字母大写
     *
     * @param param 参数
     * @return 首字母大写后的结果
     */
    public static String firstToUpperCase(String param) {
        if (Objects.isNull(param) || "".equals(param)) {
            return "";
        }
        return param.substring(0, 1).toUpperCase() + param.substring(1);
    }

}
