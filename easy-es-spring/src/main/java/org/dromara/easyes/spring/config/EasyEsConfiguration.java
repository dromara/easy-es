package org.dromara.easyes.spring.config;

import lombok.Setter;
import org.dromara.easyes.common.property.EasyEsDynamicProperties;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.dromara.easyes.common.utils.RestHighLevelClientUtils;
import org.dromara.easyes.core.index.AutoProcessIndexNotSmoothlyStrategy;
import org.dromara.easyes.core.index.AutoProcessIndexSmoothlyStrategy;
import org.dromara.easyes.spring.factory.IndexStrategyFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author MoJie
 * @since 2.0
 */
@Setter
@Configuration
public class EasyEsConfiguration implements InitializingBean {

    private EasyEsProperties easyEsProperties;

    private EasyEsDynamicProperties easyEsDynamicProperties;

    @Autowired
    public EasyEsConfiguration(EasyEsProperties easyEsProperties, EasyEsDynamicProperties easyEsDynamicProperties) {
        this.easyEsProperties = easyEsProperties;
        this.easyEsDynamicProperties = easyEsDynamicProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.easyEsProperties, "easyEsProperties must is A bean. easy-es配置类必须给配置一个bean");
    }

    @Bean
    public IndexStrategyFactory indexStrategyFactory() {
        return new IndexStrategyFactory();
    }

    @Bean
    public RestHighLevelClientUtils restHighLevelClientUtils() {
        RestHighLevelClientUtils restHighLevelClientUtils = new RestHighLevelClientUtils();
        if (this.easyEsDynamicProperties == null) {
            this.easyEsDynamicProperties = new EasyEsDynamicProperties();
        }
        Map<String, EasyEsProperties> datasourceMap = this.easyEsDynamicProperties.getDatasource();
        if (datasourceMap.isEmpty()) {
            // 设置默认数据源,兼容不使用多数据源配置场景的老用户使用习惯
            datasourceMap.put(RestHighLevelClientUtils.DEFAULT_DS, this.easyEsProperties);
        }
        for (String key : datasourceMap.keySet()) {
            EasyEsProperties easyEsConfigProperties = datasourceMap.get(key);
            RestHighLevelClientUtils.registerRestHighLevelClient(key, RestHighLevelClientUtils
                    .restHighLevelClient(easyEsConfigProperties));
        }
        return restHighLevelClientUtils;
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
