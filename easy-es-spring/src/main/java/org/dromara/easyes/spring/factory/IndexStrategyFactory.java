package org.dromara.easyes.spring.factory;

import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.enums.ProcessIndexStrategyEnum;
import org.dromara.easyes.common.utils.ExceptionUtils;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 自动托管索引策略工厂
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public class IndexStrategyFactory implements ApplicationContextAware, InitializingBean {
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
    private static final Map<Integer, AutoProcessIndexStrategy> SERVICE_MAP = new HashMap<>(DEFAULT_SIZE);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, AutoProcessIndexStrategy> beansOfType = this.applicationContext
                .getBeansOfType(AutoProcessIndexStrategy.class);
        if (beansOfType.isEmpty()) {
            throw ExceptionUtils.eee("AutoProcessIndexStrategy must have implementation. " +
                    "AutoProcessIndexStrategy索引策略接口必须要有实现并注册到容器。");
        }
        // 是否开启自动托管模式,默认开启
        ProcessIndexStrategyEnum strategy = this.applicationContext.getEnvironment()
                .getProperty(BaseEsConstants.INDEX_MODE, ProcessIndexStrategyEnum.class, ProcessIndexStrategyEnum.MANUAL);
        if (!ProcessIndexStrategyEnum.MANUAL.equals(strategy)) {
            // 将bean注册进工厂
            beansOfType.values().forEach(v -> SERVICE_MAP.putIfAbsent(v.getStrategyType(), v));
        }
    }

    public AutoProcessIndexStrategy getByStrategyType(Integer strategyType) {
        return Optional.ofNullable(SERVICE_MAP.get(strategyType))
                .orElseThrow(() -> ExceptionUtils.eee("no such service strategyType:{}", strategyType));
    }
}
