package com.xpc.easyes.autoconfig.register;

import com.xpc.easyes.core.cache.BaseCache;
import com.xpc.easyes.core.proxy.EsMapperProxy;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

/**
 * 代理类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class MapperFactoryBean<T> implements FactoryBean<T> {
    private Class<T> mapperInterface;

    @Autowired
    private RestHighLevelClient client;

    public MapperFactoryBean() {
    }

    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {
        EsMapperProxy<T> esMapperProxy = new EsMapperProxy<>(mapperInterface);
        BaseCache.initCache(mapperInterface, client);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, esMapperProxy);
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
