package org.dromara.easyes.common.utils.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;

import java.util.Optional;

/**
 * jackson自定义注解解析
 *
 * @author jaime
 * @version 1.0
 * @since 2025/2/21
 */
public class JacksonCustomAnnotationIntrospector extends JacksonAnnotationIntrospector {

    /**
     * 获取序列化和反序列化时的名称
     */
    private static PropertyName getPropertyName(Annotated m) {
        if (m instanceof AnnotatedField) {
            AnnotatedField f = (AnnotatedField) m;
            JacksonCustomConfig config = JacksonCustomConfig.jacksonConfigMap.get(f.getDeclaringClass());
            if (config != null) {
                String jsonName = config.javaJsonFieldNameMap.get(f.getName());
                if (jsonName != null) {
                    return new PropertyName(jsonName);
                }
            }
        }
        return null;
    }

    /**
     * 序列化名称
     * 序列化
     */
    @Override
    public PropertyName findNameForSerialization(Annotated m) {
        return Optional.ofNullable(getPropertyName(m)).orElse(super.findNameForSerialization(m));
    }

    /**
     * 反序列化名称
     * 反序列化
     */
    @Override
    public PropertyName findNameForDeserialization(Annotated m) {
        return Optional.ofNullable(getPropertyName(m)).orElse(super.findNameForDeserialization(m));
    }

    /**
     * 字段包含
     * 序列化 + 反序列化
     */
    @Override
    public JsonIncludeProperties.Value findPropertyInclusionByName(MapperConfig<?> c, Annotated a) {
        if (a instanceof AnnotatedClass) {
            AnnotatedClass f = (AnnotatedClass) a;
            JacksonCustomConfig config = JacksonCustomConfig.jacksonConfigMap.get(f.getAnnotated());
            if (config != null) {
                return config.allJsonField;
            }
        }
        return super.findPropertyInclusionByName(c, a);
    }

    /**
     * 时间格式
     * 序列化 + 反序列化
     */
    @Override
    public JsonFormat.Value findFormat(Annotated a) {
        if (a instanceof AnnotatedMethod) {
            AnnotatedMethod f = (AnnotatedMethod) a;
            JacksonCustomConfig config = JacksonCustomConfig.jacksonConfigMap.get(f.getDeclaringClass());
            if (config != null) {
                JsonFormat.Value value = config.formatMap.get(f.getName());
                if (value != null) {
                    return value;
                }
            }
        }
        return super.findFormat(a);
    }

    /**
     * 是否包含
     * 序列化
     */
    @Override
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        if (a instanceof AnnotatedMember) {
            AnnotatedMember f = (AnnotatedMember) a;
            JacksonCustomConfig config = JacksonCustomConfig.jacksonConfigMap.get(f.getDeclaringClass());
            if (config != null) {
                JsonInclude.Value value = config.includeMap.get(f.getName());
                if (value != null) {
                    return value;
                }
            }
        }
        return super.findPropertyInclusion(a);
    }
}