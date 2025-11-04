package org.dromara.easyes.solon.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.dromara.easyes.common.property.EasyEsDynamicProperties;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.dromara.easyes.common.utils.CollectionUtils;
import org.dromara.easyes.common.utils.EsClientUtils;
import org.dromara.easyes.core.index.AutoProcessIndexNotSmoothlyStrategy;
import org.dromara.easyes.core.index.AutoProcessIndexSmoothlyStrategy;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.Map;

/**
 * es自动配置
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Configuration
@Condition(onClass = ElasticsearchClient.class, onProperty = "${easy-es.enable:true} = true")
public class EsAutoConfiguration {

    /**
     * 加载easy-es属性变量
     * @param properties 配置
     * @return {@link EasyEsProperties}
     * @author MoJie
     */
    @Bean
    public EasyEsProperties easyEsProperties(
            @Inject(value = "${easy-es}", autoRefreshed = true) EasyEsProperties properties) {
        return properties;
    }

    /**
     * 动态数据源配置
     * @param dynamicProperties 动态数据源
     * @return {@link EasyEsDynamicProperties}
     * @author MoJie
     */
    @Bean
    public EasyEsDynamicProperties easyEsDynamicProperties(
            @Inject(value = "${easy-es.dynamic:}", autoRefreshed = true) EasyEsDynamicProperties dynamicProperties) {
        if (dynamicProperties == null) {
            return new EasyEsDynamicProperties();
        }
        return dynamicProperties;
    }

    /**
     * 装配ElasticsearchClient
     * @param easyEsProperties 配置
     * @return ElasticsearchClient bean
     */
    @Bean
    @Condition(onMissingBean = ElasticsearchClient.class)
    public ElasticsearchClient elasticClient(EasyEsProperties easyEsProperties) {
        return EsClientUtils.buildClient(easyEsProperties);
    }

    /**
     * 构建连接对象，在EasyEsProperties和EasyEsDynamicProperties构建完成后执行
     * @param properties 基础配置
     * @param dynamicProperties 动态数据源配置
     * @return {@link EsClientUtils}
     * @author MoJie
     */
    @Bean
    public EsClientUtils esClientUtils(EasyEsProperties properties, EasyEsDynamicProperties dynamicProperties) {
        EsClientUtils esClientUtils = new EsClientUtils();
        Map<String, EasyEsProperties> datasourceMap = dynamicProperties.getDatasource();
        if (CollectionUtils.isEmpty(datasourceMap)) {
            // 设置默认数据源,兼容不使用多数据源配置场景的老用户使用习惯
            datasourceMap.put(EsClientUtils.DEFAULT_DS, properties);
        }
        for (String key : datasourceMap.keySet()) {
            EasyEsProperties easyEsConfigProperties = datasourceMap.get(key);
            EsClientUtils.registerClient(key, () -> EsClientUtils.buildClient(easyEsConfigProperties));
        }
        return esClientUtils;
    }

    /**
     * 自动平滑托管索引
     * 过程零停机,数据会自动转移至新索引
     * @return {@link AutoProcessIndexStrategy}
     * @author MoJie
     */
    @Bean
    public AutoProcessIndexStrategy autoProcessIndexSmoothlyStrategy() {
        return new AutoProcessIndexSmoothlyStrategy();
    }

    /**
     * 自动平滑托管索引
     * 重建索引时原索引数据会被删除
     * @return {@link AutoProcessIndexStrategy}
     * @author MoJie
     */
    @Bean
    public AutoProcessIndexStrategy autoProcessIndexNotSmoothlyStrategy() {
        return new AutoProcessIndexNotSmoothlyStrategy();
    }

}
