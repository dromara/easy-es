package com.xpc.easyes.core.toolkit;

import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.conditions.interfaces.SFunction;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.constants.BaseEsConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 核心 处理字段名称工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldUtils {
    /**
     * 获取字段名称,如果有id,则将id转为_id
     *
     * @param func 列函数
     * @param <R>  泛型
     * @return 泛型
     */
    public static <R> String getFieldName(R func) {
        String fieldName = getFieldNameNotConvertId(func);
        if (EntityInfoHelper.getDEFAULT_ID_NAME().equals(fieldName)) {
            // id统一转为_id
            fieldName = EntityInfoHelper.getDEFAULT_ES_ID_NAME();
        }
        return fieldName;
    }

    /**
     * 获取字段名称,不转换id
     *
     * @param func 列函数
     * @param <R>  泛型
     * @return 泛型
     */
    public static <R> String getFieldNameNotConvertId(R func) {
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
     * 获取字段名
     *
     * @param func 函数
     * @param <T>  泛型
     * @return 字段名称
     */
    public static <T> String val(SFunction<T, ?> func) {
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String getter = serializedLambda.getImplMethodName();
            return resolveFieldName(getter);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new RuntimeException("no get method found!");
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

    /**
     * 获取实际字段名
     *
     * @param field            原字段名
     * @param mappingColumnMap 字段映射关系map
     * @param dbConfig         配置
     * @return 实际字段名
     */
    public static String getRealField(String field, Map<String, String> mappingColumnMap, GlobalConfig.DbConfig dbConfig) {
        String customField = mappingColumnMap.get(field);
        if (Objects.nonNull(customField)) {
            return EntityInfoHelper.getDEFAULT_ID_NAME().equals(customField) ? EntityInfoHelper.getDEFAULT_ES_ID_NAME() : customField;
        } else {
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                return StringUtils.camelToUnderline(field);
            } else {
                return field;
            }
        }
    }


    /**
     * 获取实际字段名 不转换id
     *
     * @param field            原字段名
     * @param mappingColumnMap 字段映射关系map
     * @param dbConfig         配置
     * @return 实际字段名
     */
    public static String getRealFieldNotConvertId(String field, Map<String, String> mappingColumnMap, GlobalConfig.DbConfig dbConfig) {
        String customField = mappingColumnMap.get(field);
        if (Objects.nonNull(customField)) {
            return customField;
        } else {
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                return StringUtils.camelToUnderline(field);
            } else {
                return field;
            }
        }
    }

    /**
     * 获取实际字段名数组
     *
     * @param fields           原字段名数组
     * @param mappingColumnMap 字段映射关系map
     * @param dbConfig         配置
     * @return 实际字段数组
     */
    public static String[] getRealFields(String[] fields, Map<String, String> mappingColumnMap, GlobalConfig.DbConfig dbConfig) {
        return Arrays.stream(fields)
                .map(field -> getRealField(field, mappingColumnMap, dbConfig))
                .collect(Collectors.toList())
                .toArray(new String[]{});
    }

    /**
     * 获取实际字段名数组
     *
     * @param fields           原字段名数组
     * @param mappingColumnMap 字段映射关系map
     * @return 实际字段数组
     */
    public static List<String> getRealFields(List<String> fields, Map<String, String> mappingColumnMap) {
        return Arrays.stream(getRealFields(fields.toArray(new String[0]), mappingColumnMap, GlobalConfigCache.getGlobalConfig().getDbConfig()))
                .collect(Collectors.toList());
    }

}
