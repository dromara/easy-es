package org.dromara.easyes.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.dromara.easyes.common.utils.jackson.EasyEsObjectMapperCustomizer;
import org.dromara.easyes.common.utils.jackson.JsonUtils;

/**
 * ObjectMapper是一个大概率会被使用的bean，所以将其封装一层，不直接将ObjectMapper注入容器
 */
@Getter
public class ObjectMapperBean {
    private final ObjectMapper objectMapper;

    public ObjectMapperBean(EasyEsObjectMapperCustomizer customizer,
                            EasyEsProperties easyEsProperties) {
        ObjectMapper ret;
        if (customizer != null) {
            ret = JsonUtils.buildJsonMapper(customizer);
        } else {
            String dateFormat = easyEsProperties.getGlobalConfig().getDbConfig().getDefaultDateFormat();
            ret = JsonUtils.buildJsonMapperWithDateFormat(dateFormat);
        }
        this.objectMapper = ret;
    }
}
