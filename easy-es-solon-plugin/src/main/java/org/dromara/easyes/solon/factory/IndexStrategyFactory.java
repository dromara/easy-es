package org.dromara.easyes.solon.factory;

import org.dromara.easyes.common.enums.ProcessIndexStrategyEnum;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.utils.ExceptionUtils;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.elasticsearch.client.RestHighLevelClient;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 自动托管索引策略工厂
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Component
@Condition(onBean = RestHighLevelClient.class, onProperty = "${easy-es.enable:true} = true && ${easy-es.address:x} != x")
public class IndexStrategyFactory implements LifecycleBean {

    /**
     * 配置
     */
    @Inject
    private EasyEsProperties esConfigProperties;
    /**
     * 预估初始策略工厂容量
     */
    private static final Integer DEFAULT_SIZE = 4;
    /**
     * 策略容器
     */
    private static final Map<Integer, AutoProcessIndexStrategy> SERVICE_MAP = new HashMap<>(DEFAULT_SIZE);

    /**
     * bean初始化完成后执行
     * @author MoJie
     */
    @Override
    public void start() throws Throwable {
        // 是否开启自动托管模式,默认开启
        if (!ProcessIndexStrategyEnum.MANUAL.equals(esConfigProperties.getGlobalConfig().getProcessIndexMode())) {
            // 将bean注册进工厂
            Solon.context().getBeansMapOfType(AutoProcessIndexStrategy.class)
                    .values()
                    .forEach(v -> SERVICE_MAP.putIfAbsent(v.getStrategyType(), v));
        }
    }

    public AutoProcessIndexStrategy getByStrategyType(Integer strategyType) {
        return Optional.ofNullable(SERVICE_MAP.get(strategyType))
                .orElseThrow(() -> ExceptionUtils.eee("no such service strategyType:{}", strategyType));

    }
}
