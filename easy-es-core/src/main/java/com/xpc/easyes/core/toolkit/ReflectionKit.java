package com.xpc.easyes.core.toolkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

/**
 * 反射工具类
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 处理反射时需要
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class ReflectionKit {

    private static final Map<Class, List<Field>> classFieldCache = new ConcurrentHashMap<>();

    /**
     * <p>
     * 获取该类的所有属性列表
     * </p>
     *
     * @param clazz 反射类
     */
    public static List<Field> getFieldList(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return Collections.emptyList();
        }
        List<Field> fields = classFieldCache.get(clazz);
        if (CollectionUtils.isEmpty(fields)) {
            synchronized (classFieldCache) {
                fields = doGetFieldList(clazz);
                classFieldCache.put(clazz, fields);
            }
        }
        return fields;
    }

    public static List<Field> doGetFieldList(Class<?> clazz) {
        if (clazz.getSuperclass() != null) {
            List<Field> fieldList = Stream.of(clazz.getDeclaredFields())
                    /* 过滤静态属性 */
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    /* 过滤 transient关键字修饰的属性 */
                    .filter(field -> !Modifier.isTransient(field.getModifiers()))
                    .collect(toCollection(LinkedList::new));
            /* 处理父类字段 */
            Class<?> superClass = clazz.getSuperclass();
            /* 排除重载属性 */
            return excludeOverrideSuperField(fieldList, getFieldList(superClass));
        } else {
            return Collections.emptyList();
        }
    }

    public static List<Field> excludeOverrideSuperField(List<Field> fieldList, List<Field> superFieldList) {
        // 子类属性
        Map<String, Field> fieldMap = fieldList.stream().collect(toMap(Field::getName, identity()));
        superFieldList.stream().filter(field -> !fieldMap.containsKey(field.getName())).forEach(fieldList::add);
        return fieldList;
    }

    public static Method getMethod(Class<?> cls, Field field) {
        try {
            return cls.getDeclaredMethod(ReflectionKit.getMethodCapitalize(field, field.getName()));
        } catch (NoSuchMethodException e) {
            String msg = String.format("Error: NoSuchMethod in %s.  Cause: %s", cls.getName(), e);
            throw new RuntimeException(msg);
        }
    }

    public static String getMethodCapitalize(Field field, final String str) {
        Class<?> fieldType = field.getType();
        // fix #176
        return StringUtils.concatCapitalize(boolean.class.equals(fieldType) ? "is" : "get", str);
    }
}
