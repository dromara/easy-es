package org.dromara.easyes.solon.config;

import org.dromara.easyes.common.property.EasyEsDynamicProperties;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.dromara.easyes.common.utils.*;
import org.dromara.easyes.core.index.AutoProcessIndexNotSmoothlyStrategy;
import org.dromara.easyes.core.index.AutoProcessIndexSmoothlyStrategy;
import org.elasticsearch.client.RestHighLevelClient;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.*;

/**
 * es自动配置
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Configuration
@Condition(onClass = RestHighLevelClient.class, onProperty = "${easy-es.enable:true} = true && ${easy-es.address:x} != x")
public class EsAutoConfiguration {

    @Bean
    public EasyEsProperties easyEsProperties(
            @Inject(value = "${easy-es}", autoRefreshed = true) EasyEsProperties properties) {
        return properties;
    }

    @Bean
    public EasyEsDynamicProperties easyEsDynamicProperties(
            @Inject(value = "${easy-es.dynamic:}", autoRefreshed = true) EasyEsDynamicProperties dynamicProperties) {
        if (dynamicProperties == null) {
            return new EasyEsDynamicProperties();
        }
        return dynamicProperties;
    }

    /**
     * 装配RestHighLevelClient
     *
     * @return RestHighLevelClient bean
     */
    @Bean
    @Condition(onMissingBean = RestHighLevelClient.class)
    public RestHighLevelClient restHighLevelClient(EasyEsProperties easyEsProperties) {
        return RestHighLevelClientUtils.restHighLevelClient(easyEsProperties);
    }

    @Bean
    public RestHighLevelClientUtils restHighLevelClientUtils(
            EasyEsProperties properties, EasyEsDynamicProperties dynamicProperties) {
        RestHighLevelClientUtils restHighLevelClientUtils = new RestHighLevelClientUtils();
        Map<String, EasyEsProperties> datasourceMap = dynamicProperties.getDatasource();
        if (CollectionUtils.isEmpty(datasourceMap)) {
            // 设置默认数据源,兼容不使用多数据源配置场景的老用户使用习惯
            datasourceMap.put(RestHighLevelClientUtils.DEFAULT_DS, properties);
        }
        for (String key : datasourceMap.keySet()) {
            EasyEsProperties easyEsConfigProperties = datasourceMap.get(key);
            RestHighLevelClientUtils.registerRestHighLevelClient(key, RestHighLevelClientUtils
                    .restHighLevelClient(easyEsConfigProperties));
        }
        return restHighLevelClientUtils;
    }

    @Bean
    public AutoProcessIndexStrategy autoProcessIndexSmoothlyStrategy() {
        return new AutoProcessIndexSmoothlyStrategy();
    }

    @Bean
    public AutoProcessIndexStrategy autoProcessIndexNotSmoothlyStrategy() {
        return new AutoProcessIndexNotSmoothlyStrategy();
    }

}
