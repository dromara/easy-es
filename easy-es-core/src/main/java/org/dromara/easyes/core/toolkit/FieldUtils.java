package org.dromara.easyes.core.toolkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.params.SFunction;
import org.dromara.easyes.common.property.GlobalConfig;
import org.dromara.easyes.common.utils.StringUtils;
import org.dromara.easyes.core.biz.EntityInfo;
import org.dromara.easyes.core.cache.GlobalConfigCache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.DEFAULT_ES_ID_NAME;
import static org.dromara.easyes.common.constants.BaseEsConstants.KEYWORD_SUFFIX;

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
        return getFieldNameNotConvertId(func);
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
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
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
        if (Objects.isNull(param) || param.isEmpty()) {
            return "";
        }
        return param.substring(0, 1).toUpperCase() + param.substring(1);
    }

    /**
     * 获取实际字段名
     *
     * @param field            原字段名
     * @param mappingColumnMap 字段映射关系map
     * @param entityInfo       索引信息
     * @return 实际字段名
     */
    public static String getRealField(String field, Map<String, String> mappingColumnMap, EntityInfo entityInfo) {
        String customField = mappingColumnMap.get(field);
        if (Objects.nonNull(customField)) {
            if (entityInfo.isId2Source()) {
                return customField;
            }
            // 直接用_id去查询, 但是有些版本需要开启集群配置: indices.id_field_data.enabled
            /*
             * PUT /_cluster/settings
             *   {
             *     "persistent": {
             *       "indices.id_field_data.enabled": true
             *     }
             *   }
             */
            return entityInfo.getKeyProperty().equals(field) ? DEFAULT_ES_ID_NAME : customField;
        } else {
            GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();
            if (dbConfig.isMapUnderscoreToCamelCase()) {
                return StringUtils.camelToUnderline(field);
            } else {
                return field;
            }
        }
    }

    /**
     * 获取实际字段名
     *
     * @param field            原字段名
     * @param mappingColumnMap 字段映射关系map
     * @param entityInfo       索引信息
     * @return 实际字段名
     */
    public static String getRealFieldAndSuffix(String field, Map<String, String> mappingColumnMap, EntityInfo entityInfo) {
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();
        String realField = getRealField(field, mappingColumnMap, entityInfo);
        String fieldType = entityInfo.getFieldTypeMap().get(field);
        boolean addSuffix = dbConfig.isSmartAddKeywordSuffix()
                            && FieldType.KEYWORD_TEXT.getType().equals(fieldType)
                            && !DEFAULT_ES_ID_NAME.equals(realField);
        return addSuffix ? realField + KEYWORD_SUFFIX : realField;
    }

    /**
     * 获取实际字段名 并且根据配置智能追加.keyword后缀
     *
     * @param field            字段
     * @param fieldTypeMap     字段与es字段类型映射
     * @param mappingColumnMap 实体字段与es实际字段映射
     * @param entityInfo       索引信息
     * @return 最终的字段
     */
    public static String getRealFieldAndSuffix(
            String field,
            Map<String, String> fieldTypeMap,
            Map<String, String> mappingColumnMap,
            EntityInfo entityInfo
    ) {
        GlobalConfig.DbConfig dbConfig = GlobalConfigCache.getGlobalConfig().getDbConfig();
        String realField = getRealField(field, mappingColumnMap, entityInfo);
        String fieldType = fieldTypeMap.get(field);
        boolean addSuffix = dbConfig.isSmartAddKeywordSuffix()
                            && FieldType.KEYWORD_TEXT.getType().equals(fieldType)
                            && !DEFAULT_ES_ID_NAME.equals(realField);
        if (addSuffix) {
            return realField + KEYWORD_SUFFIX;
        }
        return realField;
    }


    /**
     * 获取实际字段名 不转换id
     *
     * @param field                      原字段名
     * @param mappingColumnMap           字段映射关系map
     * @param isMapUnderscoreToCamelCase 是否开启下划线自动转驼峰
     * @return 实际字段名
     */
    public static String getRealFieldNotConvertId(String field, Map<String, String> mappingColumnMap, boolean isMapUnderscoreToCamelCase) {
        String customField = mappingColumnMap.get(field);
        if (Objects.nonNull(customField)) {
            return customField;
        } else {
            if (isMapUnderscoreToCamelCase) {
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
     * @param entityInfo       索引信息
     * @return 实际字段数组
     */
    public static List<String> getRealFields(String[] fields, Map<String, String> mappingColumnMap, EntityInfo entityInfo) {
        return Arrays.stream(fields)
                .map(field -> getRealField(field, mappingColumnMap, entityInfo))
                .collect(Collectors.toList());
    }

    /**
     * 获取实际字段名数组
     *
     * @param fields           原字段名数组
     * @param mappingColumnMap 字段映射关系map
     * @param entityInfo       索引信息
     * @return 实际字段数组
     */
    public static List<String> getRealFields(List<String> fields, Map<String, String> mappingColumnMap, EntityInfo entityInfo) {
        return fields.stream()
                .map(field -> getRealField(field, mappingColumnMap, entityInfo))
                .collect(Collectors.toList());
    }

}
