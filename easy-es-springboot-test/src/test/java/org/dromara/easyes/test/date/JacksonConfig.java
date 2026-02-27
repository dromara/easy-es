package org.dromara.easyes.test.date;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dromara.easyes.common.utils.jackson.EasyEsObjectMapperCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 测试自定义配置序列化，因为没配置时间序列化，启用将报错
 */
//@Configuration
public class JacksonConfig {
    @Bean
    public EasyEsObjectMapperCustomizer customizer() {
        return builder -> builder.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(SerializationFeature.INDENT_OUTPUT, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
