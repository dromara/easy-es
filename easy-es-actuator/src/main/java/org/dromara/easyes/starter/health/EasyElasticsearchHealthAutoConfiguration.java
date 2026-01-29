package org.dromara.easyes.starter.health;

import org.dromara.easyes.starter.health.indicator.ElasticsearchRestHealthIndicator;
import org.dromara.easyes.starter.health.properties.EasyElasticsearchHealthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.dromara.easyes.starter.health")
@EnableConfigurationProperties(EasyElasticsearchHealthProperties.class)
public class EasyElasticsearchHealthAutoConfiguration {

    /**
     * 心跳检查自动装配
     * @return 心跳检查自动装配
     */
    @Bean("elasticsearchHealthIndicator")
    @ConditionalOnProperty(value = "easy-es.health.elasticsearch.enabled",havingValue = "true",matchIfMissing = true)
    public ElasticsearchRestHealthIndicator elasticsearchHealthIndicator() {
        return new ElasticsearchRestHealthIndicator();
    }
}
