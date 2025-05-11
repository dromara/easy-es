package org.dromara.easyes.common.utils.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * jackson自定义配置
 *
 * @author jaime
 * @version 1.0
 * @since 2025/2/21
 */
@Data
public class JacksonCustomConfig {

    public static Map<Class<?>, JacksonCustomConfig> jacksonConfigMap = new HashMap<>();

    /**
     * 类名
     */
    public Class<?> clz;

    /**
     * 字段名称映射
     * java字段 - json字段
     */
    public Map<String, String> javaJsonFieldNameMap = new HashMap<>();

    /**
     * 所有json字段名称
     */
    public JsonIncludeProperties.Value allJsonField = JsonIncludeProperties.Value.all();

    /**
     * 序列化时时间格式配置
     * get/set方法名称 - 配置
     */
    public Map<String, JsonFormat.Value> formatMap = new HashMap<>();

    /**
     * 序列化时, 是否序列化
     * get方法名称/java字段名称 - 配置
     */
    public Map<String, JsonInclude.Value> includeMap = new HashMap<>();
}


