package org.dromara.easyes.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lyy
 */
@Data
@ConfigurationProperties(prefix = "easy-es.dynamic")
public class DynamicEsProperties {

    /**
     * 配置多动态数据源key datasource id
     */
    private Map<String, EasyEsConfigProperties> datasource = new HashMap<>();
}