package org.dromara.easyes.test.conf;

import org.dromara.easyes.common.utils.EasyEsHeadersCustomizer;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author MoJie
 * @since 2025-07-07
 */
@Configuration
public class EsHeaderConfig {
    @Bean
    public EasyEsHeadersCustomizer customizer() {
        return () -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("tenantId", "x-easy-es-header");
            return headers;
        };
    }
}
