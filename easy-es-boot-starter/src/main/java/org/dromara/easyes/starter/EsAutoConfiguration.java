package org.dromara.easyes.starter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.dromara.easyes.common.property.EasyEsDynamicProperties;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.utils.EsClientUtils;
import org.dromara.easyes.spring.config.EasyEsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;

/**
 * es自动配置
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Configuration
@ConditionalOnClass(ElasticsearchClient.class)
@ConditionalOnProperty(prefix = "easy-es", name = "enable", havingValue = "true", matchIfMissing = true)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)  // 确保配置类优先加载
public class EsAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "easy-es")
    public EasyEsProperties easyEsProperties() {
        return new EasyEsProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "easy-es.dynamic")
    public EasyEsDynamicProperties easyEsDynamicProperties() {
        return new EasyEsDynamicProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn({"easyEsProperties", "easyEsDynamicProperties"})  // 显式依赖
    public ElasticsearchClient elasticClient() {
        return EsClientUtils.buildClient(easyEsProperties());
    }

    @Bean
    public EasyEsConfiguration esConfiguration() {
        return new EasyEsConfiguration();
    }

}
