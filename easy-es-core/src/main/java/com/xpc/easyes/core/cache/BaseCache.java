package com.xpc.easyes.core.cache;

import com.xpc.easyes.core.conditions.BaseEsMapperImpl;
import com.xpc.easyes.core.toolkit.ExceptionUtils;
import com.xpc.easyes.core.toolkit.FieldUtils;
import com.xpc.easyes.core.toolkit.TypeUtils;
import org.elasticsearch.client.RestHighLevelClient;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
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
     */
    public static void initCache(Class<?> mapperInterface, RestHighLevelClient client) {
        // 初始化baseEsMapper的所有实现类实例
        BaseEsMapperImpl baseEsMapper = new BaseEsMapperImpl();
        baseEsMapper.setClient(client);
        Class<?> entityClass = TypeUtils.getInterfaceT(mapperInterface, 0);
        baseEsMapper.setEntityClass(entityClass);
        baseEsMapperInstanceMap.put(mapperInterface, baseEsMapper);

        // 初始化entity中所有字段(注解策略生效)
        Method[] entityMethods = entityClass.getMethods();
        Map<String, Method> invokeMethodsMap = new ConcurrentHashMap<>(entityMethods.length);
        Arrays.stream(entityMethods)
                .forEach(entityMethod -> {
                    String methodName = entityMethod.getName();
                    if (methodName.startsWith(GET_FUNC_PREFIX) || methodName.startsWith(IS_FUNC_PREFIX)
                            || methodName.startsWith(SET_FUNC_PREFIX)) {
                        invokeMethodsMap.put(FieldUtils.resolveFieldName(methodName), entityMethod);
                    }
                });
        baseEsEntityMethodMap.putIfAbsent(entityClass, invokeMethodsMap);
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
     * 获取缓存中对应的entity的所有字段(字段注解策略生效)
     *
     * @param entityClass 实体
     * @param methodName  方法名
     * @return 执行方法
     */
    public static Method getEsEntityInvokeMethod(Class<?> entityClass, String methodName) {
        return Optional.ofNullable(baseEsEntityMethodMap.get(entityClass))
                .map(b -> b.get(methodName))
                .orElseThrow(() -> ExceptionUtils.eee("no such method:", entityClass, methodName));
    }
}
