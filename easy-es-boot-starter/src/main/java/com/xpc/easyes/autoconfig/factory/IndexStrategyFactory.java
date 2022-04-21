package com.xpc.easyes.autoconfig.factory;


import com.xpc.easyes.autoconfig.config.EasyEsConfigProperties;
import com.xpc.easyes.autoconfig.service.AutoProcessIndexService;
import com.xpc.easyes.core.enums.ProcessIndexStrategyEnum;
import com.xpc.easyes.core.toolkit.ExceptionUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 自动托管索引策略工厂
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Component
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnProperty(prefix = "easy-es", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class IndexStrategyFactory implements ApplicationContextAware, InitializingBean {
    /**
     * 配置
     */
    @Autowired
    private EasyEsConfigProperties esConfigProperties;
    /**
     * 预估初始策略工厂容量
     */
    private static final Integer DEFAULT_SIZE = 4;
    /**
     * spring上下文
     */
    private ApplicationContext applicationContext;
    /**
     * 策略容器
     */
    private static final Map<Integer, AutoProcessIndexService> SERVICE_MAP = new HashMap<>(DEFAULT_SIZE);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        // 是否开启自动托管模式,默认开启
        if (!ProcessIndexStrategyEnum.MANUAL.equals(esConfigProperties.getGlobalConfig().getProcessIndexMode())) {
            // 将bean注册进工厂
            applicationContext.getBeansOfType(AutoProcessIndexService.class)
                    .values()
                    .forEach(v -> SERVICE_MAP.putIfAbsent(v.getStrategyType(), v));
        }
    }

    public AutoProcessIndexService getByStrategyType(Integer strategyType) {
        return Optional.ofNullable(SERVICE_MAP.get(strategyType))
                .orElseThrow(() -> ExceptionUtils.eee("no such service strategyType:{}", strategyType));

    }
}
