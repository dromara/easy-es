package com.xpc.easyes.autoconfig.register;

import com.xpc.easyes.autoconfig.config.EasyEsConfigProperties;
import com.xpc.easyes.autoconfig.factory.IndexStrategyFactory;
import com.xpc.easyes.autoconfig.service.AutoProcessIndexService;
import com.xpc.easyes.core.cache.BaseCache;
import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.enums.ProcessIndexStrategyEnum;
import com.xpc.easyes.core.proxy.EsMapperProxy;
import com.xpc.easyes.core.toolkit.LogUtils;
import com.xpc.easyes.core.toolkit.TypeUtils;
import com.xpc.easyes.extension.anno.Intercepts;
import com.xpc.easyes.extension.plugins.Interceptor;
import com.xpc.easyes.extension.plugins.InterceptorChain;
import com.xpc.easyes.extension.plugins.InterceptorChainHolder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import sun.dc.pr.PRError;

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
    private IndexStrategyFactory indexStrategyFactory;

    @Autowired
    private EasyEsConfigProperties esConfigProperties;

    public MapperFactoryBean() {
    }

    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {

        EsMapperProxy<T> esMapperProxy = new EsMapperProxy<>(mapperInterface);

        // 获取实体类
        Class<?> entityClass = TypeUtils.getInterfaceT(mapperInterface, 0);

        // 初始化缓存
        BaseCache.initCache(mapperInterface, entityClass, client);
        GlobalConfigCache.setGlobalConfig(esConfigProperties.getGlobalConfig());

        // 创建代理
        T t = (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, esMapperProxy);

        // 初始化拦截器链
        InterceptorChain interceptorChain = this.initInterceptorChain();

        // 异步处理索引创建/更新/数据迁移等
        GlobalConfig globalConfig = esConfigProperties.getGlobalConfig();
        if (!ProcessIndexStrategyEnum.MANUAL.equals(globalConfig.getProcessIndexMode())) {
            AutoProcessIndexService autoProcessIndexService = indexStrategyFactory
                    .getByStrategyType(globalConfig.getProcessIndexMode().getStrategyType());
            autoProcessIndexService.processIndexAsync(entityClass, client);
        }else {
            LogUtils.info("===> manual index mode activated");
        }
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

    private InterceptorChain initInterceptorChain() {
        InterceptorChainHolder interceptorChainHolder = InterceptorChainHolder.getInstance();
        InterceptorChain interceptorChain = interceptorChainHolder.getInterceptorChain();
        if (interceptorChain == null) {
            synchronized (this) {
                interceptorChainHolder.initInterceptorChain();
                Map<String, Object> beansWithAnnotation = this.applicationContext.getBeansWithAnnotation(Intercepts.class);
                beansWithAnnotation.forEach((key, val) -> {
                    if (val instanceof Interceptor) {
                        Interceptor interceptor = (Interceptor) val;
                        interceptorChainHolder.addInterceptor(interceptor);
                    }
                });
            }
        }
        return interceptorChainHolder.getInterceptorChain();
    }

}
