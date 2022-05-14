package com.xpc.easyes.core.cache;

import com.xpc.easyes.core.conditions.BaseEsMapperImpl;
import com.xpc.easyes.core.toolkit.CollectionUtils;
import com.xpc.easyes.core.toolkit.EntityInfoHelper;
import com.xpc.easyes.core.toolkit.ExceptionUtils;
import com.xpc.easyes.core.toolkit.FieldUtils;
import org.elasticsearch.client.RestHighLevelClient;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.xpc.easyes.core.constants.BaseEsConstants.*;

/**
 * 基本缓存
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class BaseCache {
    /**
     * 用于存放BaseEsMapper的所有实例
     */
    private static final Map<Class<?>, BaseEsMapperImpl<?>> baseEsMapperInstanceMap = new ConcurrentHashMap<>();
    /**
     * 用于存放Es entity 中的字段的get/is方法
     */
    private static final Map<Class<?>, Map<String, Method>> baseEsEntityMethodMap = new ConcurrentHashMap<>();

    /**
     * 初始化缓存
     *
     * @param mapperInterface mapper接口
     * @param client          es客户端
     * @param entityClass     实体类
     */
    public static void initCache(Class<?> mapperInterface, Class<?> entityClass, RestHighLevelClient client) {
        // 初始化baseEsMapper的所有实现类实例
        BaseEsMapperImpl baseEsMapper = new BaseEsMapperImpl();
        baseEsMapper.setClient(client);

        baseEsMapper.setEntityClass(entityClass);
        baseEsMapperInstanceMap.put(mapperInterface, baseEsMapper);

        // 初始化entity中所有字段(注解策略生效)
        Map<String, Method> invokeMethodsMap = initInvokeMethodsMap(entityClass);
        baseEsEntityMethodMap.putIfAbsent(entityClass, invokeMethodsMap);

        // 初始化嵌套类中的所有方法
        Set<Class<?>> allNestedClass = EntityInfoHelper.getEntityInfo(entityClass).getAllNestedClass();
        if (CollectionUtils.isNotEmpty(allNestedClass)) {
            allNestedClass.forEach(nestedClass -> {
                Map<String, Method> nestedInvokeMethodsMap = initInvokeMethodsMap(nestedClass);
                baseEsEntityMethodMap.putIfAbsent(nestedClass, nestedInvokeMethodsMap);
            });

        }
    }

    private static Map<String, Method> initInvokeMethodsMap(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Map<String, Method> invokeMethodsMap = new ConcurrentHashMap<>(methods.length);
        Arrays.stream(methods)
                .forEach(entityMethod -> {
                    String methodName = entityMethod.getName();
                    if (methodName.startsWith(GET_FUNC_PREFIX) || methodName.startsWith(IS_FUNC_PREFIX)
                            || methodName.startsWith(SET_FUNC_PREFIX)) {
                        invokeMethodsMap.put(methodName, entityMethod);
                    }
                });
        return invokeMethodsMap;
    }

    /**
     * 获取缓存中对应的BaseEsMapperImpl
     *
     * @param mapperInterface mapper接口
     * @return 实现类
     */
    public static BaseEsMapperImpl<?> getBaseEsMapperInstance(Class<?> mapperInterface) {
        return Optional.ofNullable(baseEsMapperInstanceMap.get(mapperInterface))
                .orElseThrow(() -> ExceptionUtils.eee("no such instance", mapperInterface));
    }

    /**
     * 获取缓存中对应entity和methodName的getter方法
     *
     * @param entityClass 实体
     * @param methodName  方法名
     * @return 执行方法
     */
    public static Method getterMethod(Class<?> entityClass, String methodName) {
        return Optional.ofNullable(baseEsEntityMethodMap.get(entityClass))
                .map(b -> b.get(GET_FUNC_PREFIX + FieldUtils.firstToUpperCase(methodName)))
                .orElseThrow(() -> ExceptionUtils.eee("no such method:", entityClass, methodName));
    }

    /**
     * 获取缓存中对应entity和methodName的setter方法
     *
     * @param entityClass 实体
     * @param methodName  方法名
     * @return 执行方法
     */
    public static Method setterMethod(Class<?> entityClass, String methodName) {
        return Optional.ofNullable(baseEsEntityMethodMap.get(entityClass))
                .map(b -> b.get(SET_FUNC_PREFIX + FieldUtils.firstToUpperCase(methodName)))
                .orElseThrow(() -> ExceptionUtils.eee("no such method:", entityClass, methodName));
    }
}
