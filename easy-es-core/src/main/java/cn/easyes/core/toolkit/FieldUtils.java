package cn.easyes.core.toolkit;

import cn.easyes.common.constants.BaseEsConstants;
import cn.easyes.common.params.SFunction;
import cn.easyes.common.utils.StringUtils;
import cn.easyes.core.cache.GlobalConfigCache;
import cn.easyes.core.config.GlobalConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.easyes.common.constants.BaseEsConstants.DEFAULT_ES_ID_NAME;
import static cn.easyes.common.constants.BaseEsConstants.DEFAULT_ID_NAME;

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
        if (DEFAULT_ID_NAME.equals(fieldName)) {
            // id统一转为_id
            fieldName = DEFAULT_ES_ID_NAME;
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
            String getter;
            if (func instanceof Proxy) {
                InvocationHandler handler = Proxy.getInvocationHandler(func);
                Field field = handler.getClass().getDeclaredField("val$target");
                field.setAccessible(Boolean.TRUE);
                MethodHandle dmh = (MethodHandle) field.get(handler);
                Executable executable = MethodHandles.reflectAs(Executable.class, dmh);
                getter = executable.getName();
            } else {
                Method method = func.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(Boolean.TRUE);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
                getter = serializedLambda.getImplMethodName();
            }
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
            return DEFAULT_ID_NAME.equals(customField) ? DEFAULT_ES_ID_NAME : customField;
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
