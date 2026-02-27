package org.dromara.easyes.spring.config;

import lombok.NonNull;
import lombok.Setter;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.property.EasyEsDynamicProperties;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.dromara.easyes.common.utils.EasyEsHeadersCustomizer;
import org.dromara.easyes.common.utils.EsClientUtils;
import org.dromara.easyes.common.utils.jackson.EasyEsObjectMapperCustomizer;
import org.dromara.easyes.core.config.ObjectMapperBean;
import org.dromara.easyes.core.index.AutoProcessIndexNotSmoothlyStrategy;
import org.dromara.easyes.core.index.AutoProcessIndexSmoothlyStrategy;
import org.dromara.easyes.spring.factory.IndexStrategyFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Easy-Es Spring配置类
 * @author MoJie
 * @since 2.0
 */
@Configuration
@Conditional(EasyEsConfiguredCondition.class)
public class EasyEsConfiguration implements InitializingBean, EnvironmentAware {

    private Environment environment;

    @Setter
    @Autowired(required = false)
    private EasyEsProperties easyEsProperties;

    @Setter
    @Autowired(required = false)
    private EasyEsDynamicProperties easyEsDynamicProperties;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    /**
     * 当当前配置类注册为bean完成后触发，校验easy-es配置是否存在，
     * 如果easy-es.enable: false, 那么不进行校验和抛出异常
     * 默认情况下引入了easy-es是需要配置的，即easy-es.enable:true
     * 如果不需要easy-es，那么自行配置为false
     * @author MoJie
     */
    @Override
    public void afterPropertiesSet() {
        Boolean enable = environment.getProperty(BaseEsConstants.ENABLE_PREFIX, Boolean.class, Boolean.TRUE);
        if (enable) {
            Assert.notNull(this.easyEsProperties, "easyEsProperties must is A bean. easy-es配置类必须给配置一个bean");
        }
    }

    @Bean
    public IndexStrategyFactory indexStrategyFactory() {
        return new IndexStrategyFactory();
    }

    @Bean
    public ObjectMapperBean objectMapperBean(@Autowired(required = false) EasyEsObjectMapperCustomizer customizer) {
        return new ObjectMapperBean(customizer, easyEsProperties);
    }

    @Bean
    public EsClientUtils esClientUtils(ObjectMapperBean objectMapperBean, @Autowired(required = false) EasyEsHeadersCustomizer easyEsHeadersCustomizer) {
        EsClientUtils esClientUtils = new EsClientUtils();
        if (this.easyEsDynamicProperties == null) {
            this.easyEsDynamicProperties = new EasyEsDynamicProperties();
        }
        Map<String, EasyEsProperties> datasourceMap = this.easyEsDynamicProperties.getDatasource();
        if (datasourceMap.isEmpty()) {
            // 设置默认数据源,兼容不使用多数据源配置场景的老用户使用习惯
            datasourceMap.put(EsClientUtils.DEFAULT_DS, this.easyEsProperties);
        }
        for (String key : datasourceMap.keySet()) {
            EasyEsProperties easyEsConfigProperties = datasourceMap.get(key);
            EsClientUtils.registerClient(key,
                    () -> EsClientUtils.buildClient(easyEsConfigProperties, objectMapperBean.getObjectMapper(), easyEsHeadersCustomizer));
        }
        return esClientUtils;
    }

    /**
     * 索引策略注册
     *
     * @return {@link AutoProcessIndexStrategy}
     * @author MoJie
     */
    @Bean
    public AutoProcessIndexStrategy autoProcessIndexSmoothlyStrategy() {
        return new AutoProcessIndexSmoothlyStrategy();
    }

    @Bean
    public AutoProcessIndexStrategy autoProcessIndexNotSmoothlyStrategy() {
        return new AutoProcessIndexNotSmoothlyStrategy();
    }

}
