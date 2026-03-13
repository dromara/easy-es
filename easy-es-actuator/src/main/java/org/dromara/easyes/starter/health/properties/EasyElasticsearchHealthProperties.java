package org.dromara.easyes.starter.health.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "easy-es.health.elasticsearch")
public class EasyElasticsearchHealthProperties {
    /**
     * 心跳检查是否开启，默认：开启
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
