package com.xpc.easyes.autoconfig.factory;


import com.xpc.easyes.autoconfig.service.AutoProcessIndexService;
import com.xpc.easyes.core.enums.ProcessIndexStrategyEnum;
import com.xpc.easyes.core.toolkit.ExceptionUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.xpc.easyes.autoconfig.constants.PropertyKeyConstants.PROCESS_INDEX_MODE;

/**
 * 自动托管索引策略工厂
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Component
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnProperty(prefix = "easy-es", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class IndexStrategyFactory implements ApplicationContextAware, InitializingBean, EnvironmentAware {
    /**
     * 预估初始策略工厂容量
     */
    private static final Integer DEFAULT_SIZE = 4;
    private ApplicationContext applicationContext;
    private Environment environment;
    /**
     * 策略容器
     */
    private static final Map<Integer, AutoProcessIndexService> SERVICE_MAP = new HashMap<>(DEFAULT_SIZE);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 默认开启
        String mode = environment.getProperty(PROCESS_INDEX_MODE);
        if (!ProcessIndexStrategyEnum.MANUAL.getValue().equalsIgnoreCase(mode)) {
            applicationContext.getBeansOfType(AutoProcessIndexService.class)
                    .values()
                    .forEach(v -> SERVICE_MAP.putIfAbsent(v.getStrategyType(), v));
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public AutoProcessIndexService getByStrategyType(Integer strategyType) {
        return Optional.ofNullable(SERVICE_MAP.get(strategyType))
                .orElseThrow(() -> ExceptionUtils.eee("no such service strategyType:{}", strategyType));

    }
}
