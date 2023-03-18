package cn.easyes.core.proxy;

import cn.easyes.core.cache.BaseCache;
import cn.easyes.core.core.BaseEsMapperImpl;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 代理类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class EsMapperProxy<T> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1L;
    private Class<T> mapperInterface;

    public EsMapperProxy(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        BaseEsMapperImpl<?> baseEsMapperInstance = BaseCache.getBaseEsMapperInstance(mapperInterface);
        // 这里如果后续需要像MP那样 从xml生成代理的其它方法,则可增强method,此处并不需要
        return method.invoke(baseEsMapperInstance, args);
    }

}
