package org.dromara.easyes.common.utils.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dromara.easyes.common.utils.StringUtils;
import org.dromara.easyes.common.utils.jackson.deserializer.DateDeserializer;
import org.dromara.easyes.common.utils.jackson.deserializer.LocalDateDeserializer;
import org.dromara.easyes.common.utils.jackson.deserializer.LocalDateTimeDeserializer;
import org.dromara.easyes.common.utils.jackson.serializer.DateSerializer;
import org.dromara.easyes.common.utils.jackson.serializer.LocalDateSerializer;
import org.dromara.easyes.common.utils.jackson.serializer.LocalDateTimeSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON工具类
 *
 * @author jaime
 * @version 1.0
 * @since 2023/08/09
 */
public class JsonUtils {

    /**
     * 默认jackson配置
     */
    private static final ObjectMapper objectMapper = base();
    private static final DefaultPrettyPrinter DEFAULT_PRETTY_PRINTER = new DefaultPrettyPrinter();

    /**
     * 框架内部使用
     * @return
     */
    private static JsonMapper base() {
        return buildJsonMapper(builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
            javaTimeModule.addSerializer(Date.class, new DateSerializer());
            javaTimeModule.addDeserializer(Date.class, new DateDeserializer());
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer());
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());

            builder.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                    .configure(SerializationFeature.INDENT_OUTPUT, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .addModule(javaTimeModule);
        });
    }

    public static JsonMapper buildJsonMapper(EasyEsObjectMapperCustomizer consumer) {
        JsonMapper.Builder builder = JsonMapper.builder();
        consumer.customize(builder);
        JsonMapper jsonMapper = builder.build();

        // 必须执行，用于指定序列化后的字段名称，日期格式
        AnnotationIntrospector anno = jsonMapper.getSerializerProviderInstance().getAnnotationIntrospector();
        jsonMapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(anno, new JacksonCustomAnnotationIntrospector()));
        return jsonMapper;
    }

    public static JsonMapper buildJsonMapperWithDateFormat(String dateFormat) {
        return buildJsonMapper(builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateFormat));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateFormat));
            javaTimeModule.addSerializer(Date.class, new DateSerializer(dateFormat));
            javaTimeModule.addDeserializer(Date.class, new DateDeserializer(dateFormat));
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormat));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormat));

            builder.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                    .configure(SerializationFeature.INDENT_OUTPUT, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .addModule(javaTimeModule);
        });
    }

    public static String toJsonStr(Object data) {
        return toJsonStr(objectMapper, data);
    }

    public static byte[] toBytes(Object data) {
        return toBytes(objectMapper, data);
    }

    public static String toJsonPrettyStr(Object data) {
        return toJsonPrettyStr(objectMapper, data);
    }

    public static <T> T toBean(String json, Class<T> type) {
        return toBean(objectMapper, json, type);
    }

    public static <T> T toBean(String json, TypeReference<T> type) {
        return toBean(objectMapper, json, type);
    }

    public static <T> T toBean(String json, Type type) {
        return toBean(objectMapper, json, type);
    }

    public static <V> List<V> toList(String json, Class<V> v) {
        return toList(objectMapper, json, v);
    }


    public static <V> Set<V> toSet(String json, Class<V> v) {
        return toSet(objectMapper, json, v);
    }


    public static <K, V> Map<K, V> toMap(String json, Class<K> k, Class<V> v) {
        return toMap(objectMapper, json, k, v);
    }

    public static <T> T toCollection(String json, Class<?> parametrized, Class<?>... parameterClasses) throws Exception {
        return toCollection(objectMapper, json, parametrized, parameterClasses);
    }

    public static JsonNode readTree(String json) {
        return readTree(objectMapper, json);
    }

    public static String toJsonStr(ObjectMapper om, Object data) {
        try {
            if (data == null) {
                return null;
            }
            return om.writeValueAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] toBytes(ObjectMapper om, Object data) {
        try {
            if (data == null) {
                return null;
            }
            return om.writeValueAsBytes(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String toJsonPrettyStr(ObjectMapper om, Object data) {
        try {
            if (data == null) {
                return null;
            }
            return om.writer(DEFAULT_PRETTY_PRINTER).writeValueAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static <T> T toBean(ObjectMapper om, String json, Class<T> type) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return om.readValue(json, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static <T> T toBean(ObjectMapper om, String json, TypeReference<T> type) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return om.readValue(json, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static <T> T toBean(ObjectMapper om, String json, Type type) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return om.readValue(json, TypeFactory.defaultInstance().constructType(type));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <V> List<V> toList(ObjectMapper om, String json, Class<V> v) {
        try {
            return toCollection(om, json, List.class, v);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <V> Set<V> toSet(ObjectMapper om, String json, Class<V> v) {
        try {
            return toCollection(om, json, Set.class, v);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <K, V> Map<K, V> toMap(ObjectMapper om, String json, Class<K> k, Class<V> v) {
        try {
            return toCollection(om, json, Map.class, k, v);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T toCollection(ObjectMapper om, String json, Class<?> parametrized, Class<?>... parameterClasses) throws Exception {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        JavaType type = om.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        return om.readValue(json, type);
    }

    public static JsonNode readTree(ObjectMapper om, String json) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return om.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
