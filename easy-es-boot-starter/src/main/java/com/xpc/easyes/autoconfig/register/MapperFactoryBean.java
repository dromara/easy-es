package com.xpc.easyes.autoconfig.register;

import com.xpc.easyes.autoconfig.config.EsConfigProperties;
import com.xpc.easyes.core.cache.BaseCache;
import com.xpc.easyes.core.proxy.EsMapperProxy;
import com.xpc.easyes.extension.anno.Intercepts;
import com.xpc.easyes.extension.plugins.Interceptor;
import com.xpc.easyes.extension.plugins.InterceptorChain;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 代理类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class MapperFactoryBean<T> implements FactoryBean<T> {
    private Class<T> mapperInterface;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EsConfigProperties esConfigProperties;

    public MapperFactoryBean() {
    }

    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {
        EsMapperProxy<T> esMapperProxy = new EsMapperProxy<>(mapperInterface);
        BaseCache.initCache(mapperInterface, client);
        T t = (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, esMapperProxy);
        InterceptorChain interceptorChain = this.initInterceptorChain();
        return interceptorChain.pluginAll(t);
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private InterceptorChain initInterceptorChain(){
        InterceptorChain interceptorChain = esConfigProperties.getInterceptorChain();
        if(interceptorChain == null){
            synchronized (this){
                esConfigProperties.initInterceptorChain();
                Map<String, Object> beansWithAnnotation = this.applicationContext.getBeansWithAnnotation(Intercepts.class);
                if(beansWithAnnotation != null){
                    beansWithAnnotation.forEach((key, val) ->{
                        if(val instanceof Interceptor){
                            Interceptor interceptor = (Interceptor) val;
                            esConfigProperties.addInterceptor(interceptor);
                        }
                    });
                }
            }
        }
        return esConfigProperties.getInterceptorChain();
    }

}
