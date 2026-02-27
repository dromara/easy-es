package org.dromara.easyes.common.utils.jackson;

import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * 用户自定义Jackson配置
 */
@FunctionalInterface
public interface EasyEsObjectMapperCustomizer {
	void customize(JsonMapper.Builder builder);
}