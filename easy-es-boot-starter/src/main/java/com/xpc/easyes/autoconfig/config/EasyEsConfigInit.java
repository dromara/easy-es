package com.xpc.easyes.autoconfig.config;

import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.toolkit.LogUtils;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * easy-es配置项初始化
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Order(Integer.MIN_VALUE)
@Configuration
@ConditionalOnClass(RestHighLevelClient.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EasyEsConfigInit implements InitializingBean {
    private final EasyEsConfigProperties esConfigProperties;

    @Override
    public void afterPropertiesSet() {
        LogUtils.info("===> Easy-Es Global config init");
        GlobalConfigCache.setGlobalConfig(esConfigProperties.getGlobalConfig());
    }
}
