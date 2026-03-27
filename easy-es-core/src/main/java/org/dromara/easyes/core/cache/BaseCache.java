package org.dromara.easyes.core.cache;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.utils.CollectionUtils;
import org.dromara.easyes.common.utils.ExceptionUtils;
import org.dromara.easyes.core.biz.EntityInfo;
import org.dromara.easyes.core.kernel.BaseEsMapperImpl;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.FieldUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * 初始化mapper缓存
     * @param mapperInterface mapper接口
     * @param entityClass 实体类
     * @param client es客户端
     * @param <T> 泛型
     */
    public static <T> void initMapperCache(Class<?> mapperInterface, Class<T> entityClass, ElasticsearchClient client) {
        // 初始化baseEsMapper的所有实现类实例
        BaseEsMapperImpl<T> baseEsMapper = new BaseEsMapperImpl<>();
        baseEsMapper.setClient(client);

        baseEsMapper.setEntityClass(entityClass);
        baseEsMapperInstanceMap.put(mapperInterface, baseEsMapper);
    }

    /**
     * 初始化entity缓存
     *
     * @param entityClass 实体类
     */
    public static void initEntityCache(Class<?> entityClass) {
        // 初始化entity中所有字段(注解策略生效)
        Map<String, Method> invokeMethodsMap = initInvokeMethodsMap(entityClass);
        baseEsEntityMethodMap.putIfAbsent(entityClass, invokeMethodsMap);

        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        // 初始化嵌套类中的所有方法
        Set<Class<?>> allNestedClass = entityInfo.getAllNestedOrObjectClass();
        if (CollectionUtils.isNotEmpty(allNestedClass)) {
            allNestedClass.forEach(nestedClass -> {
                Map<String, Method> nestedInvokeMethodsMap = initInvokeMethodsMap(nestedClass);
                baseEsEntityMethodMap.putIfAbsent(nestedClass, nestedInvokeMethodsMap);
            });
        }

        // 初始化父子类型JoinField中的所有方法
        Map<String, Method> joinInvokeMethodsMap = initInvokeMethodsMap(entityInfo.getJoinFieldClass());
        BaseCache.baseEsEntityMethodMap.putIfAbsent(entityInfo.getJoinFieldClass(), joinInvokeMethodsMap);
    }


    /**
     * 初始化get及set方法容器
     *
     * @param clazz 类
     * @return 指定类的get及set方法容器
     */
    private static Map<String, Method> initInvokeMethodsMap(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Map<String, Method> invokeMethodsMap = new ConcurrentHashMap<>(methods.length);
        Arrays.stream(methods)
                .forEach(entityMethod -> {
                    String methodName = entityMethod.getName();
                    if (methodName.startsWith(BaseEsConstants.GET_FUNC_PREFIX) || methodName.startsWith(BaseEsConstants.IS_FUNC_PREFIX)
                        || methodName.startsWith(BaseEsConstants.SET_FUNC_PREFIX)) {
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
        return Optional.ofNullable(BaseCache.baseEsEntityMethodMap.get(entityClass))
                .map(b -> {
                    Method method = b.get(BaseEsConstants.GET_FUNC_PREFIX + FieldUtils.firstToUpperCase(methodName));
                    if (Objects.isNull(method)) {
                        // 兼容基本数据类型boolean
                        method = b.get(BaseEsConstants.IS_FUNC_PREFIX + FieldUtils.firstToUpperCase(methodName));
                    }
                    return method;
                }).orElseThrow(() -> ExceptionUtils.eee("no such method, entityClass: %s, method:%s", entityClass, methodName));
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
                .map(b -> b.get(BaseEsConstants.SET_FUNC_PREFIX + FieldUtils.firstToUpperCase(methodName)))
                .orElseThrow(() -> ExceptionUtils.eee("no such method:%s-%s", entityClass, methodName));
    }

    /**
     * 执行缓存中对应entity和methodName的setter方法
     *
     * @param entityClass 实体
     * @param methodName  方法名
     * @param obj         对象
     * @param value       值
     */
    public static void setterInvoke(Class<?> entityClass, String methodName, Object obj, Object value) {
        try {
            setterMethod(entityClass, methodName).invoke(obj, value);
        } catch (Exception e) {
            throw ExceptionUtils.eee("setterMethod exception", e);
        }
    }

    /**
     * 执行缓存中对应entity和methodName的getter方法
     *
     * @param entityClass 实体
     * @param methodName  方法名
     * @param obj         对象
     * @return 执行方法
     */
    public static Object getterInvoke(Class<?> entityClass, String methodName, Object obj) {
        try {
            return getterMethod(entityClass, methodName).invoke(obj);
        } catch (Exception e) {
            throw ExceptionUtils.eee("getterInvoke exception", e);
        }
    }

    /**
     * 获取id
     *
     * @param entityClass 实体
     * @param obj         对象
     * @return 执行方法
     */
    public static Object getId(Class<?> entityClass, Object obj) {
        String idFieldName = EntityInfoHelper.getEntityInfo(entityClass).getKeyProperty();
        return getterInvoke(entityClass, idFieldName, obj);
    }

    /**
     * 获取所有BaseEsMapperImpl实例
     * @return 实例集合
     */
    public static Collection<BaseEsMapperImpl<?>> getAllImpl() {
        return baseEsMapperInstanceMap.values();
    }
}
