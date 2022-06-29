package cn.easyes.starter.register;

import cn.easyes.annotation.Intercepts;
import cn.easyes.common.enums.ProcessIndexStrategyEnum;
import cn.easyes.common.params.DefaultChildClass;
import cn.easyes.common.utils.LogUtils;
import cn.easyes.common.utils.TypeUtils;
import cn.easyes.core.biz.EntityInfo;
import cn.easyes.core.cache.BaseCache;
import cn.easyes.core.cache.GlobalConfigCache;
import cn.easyes.core.config.GlobalConfig;
import cn.easyes.core.proxy.EsMapperProxy;
import cn.easyes.core.toolkit.EntityInfoHelper;
import cn.easyes.extension.context.Interceptor;
import cn.easyes.extension.context.InterceptorChain;
import cn.easyes.extension.context.InterceptorChainHolder;
import cn.easyes.starter.config.EasyEsConfigProperties;
import cn.easyes.starter.factory.IndexStrategyFactory;
import cn.easyes.starter.service.AutoProcessIndexService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

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
        GlobalConfigCache.setGlobalConfig(esConfigProperties.getGlobalConfig());
        BaseCache.initCache(mapperInterface, entityClass, client);

        // 创建代理
        T t = (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, esMapperProxy);

        // 初始化拦截器链
        InterceptorChain interceptorChain = this.initInterceptorChain();

        // 异步处理索引创建/更新/数据迁移等
        GlobalConfig globalConfig = esConfigProperties.getGlobalConfig();
        if (!ProcessIndexStrategyEnum.MANUAL.equals(globalConfig.getProcessIndexMode())) {
            // 父子类型,仅针对父类型创建索引,子类型不创建索引
            EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
            if (!entityInfo.isChild()) {
                AutoProcessIndexService autoProcessIndexService = indexStrategyFactory
                        .getByStrategyType(globalConfig.getProcessIndexMode().getStrategyType());
                autoProcessIndexService.processIndexAsync(entityClass, client);

                // 将子文档索引激活为父文档索引
                if (!DefaultChildClass.class.equals(entityInfo.getChildClass())) {
                  Optional.ofNullable(entityInfo.getChildClass())
                          .flatMap(childClass -> Optional.ofNullable(EntityInfoHelper.getEntityInfo(childClass)))
                          .ifPresent(childEntityInfo -> childEntityInfo.setIndexName(entityInfo.getIndexName()));
                }
            }
        } else {
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
