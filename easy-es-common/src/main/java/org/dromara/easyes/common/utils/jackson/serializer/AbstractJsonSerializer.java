package org.dromara.easyes.common.utils.jackson.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.dromara.easyes.common.utils.ExceptionUtils;
import org.dromara.easyes.common.utils.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

public abstract class AbstractJsonSerializer<T, D extends AbstractJsonSerializer<T, D>> extends JsonSerializer<T> implements ContextualSerializer {
    private static final Map<Class<?>, Map<String, AbstractJsonSerializer<?, ?>>> CACHE = new ConcurrentHashMap<>();

    protected final String pattern;

    public AbstractJsonSerializer() {
        this(DEFAULT_DATE_TIME_FORMAT);
    }

    public AbstractJsonSerializer(String pattern) {
        if (ES_DEFAULT_DATE_TIME_FORMAT.equals(pattern)) {
            pattern = UTC_WITH_XXX_OFFSET_TIME_FORMAT;
        }
        this.pattern = pattern;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeObject(null);
        } else {
            try {
                String format = format(value);
                gen.writeString(format);
            } catch (Exception e) {
                throw ExceptionUtils.eee("serializer date fail：%s", e, value.toString());
            }
        }
    }

    abstract protected String format(T date);

    abstract protected D newInstance(String pattern);

    @SuppressWarnings("unchecked")
    @Override
    public JsonSerializer<T> createContextual(SerializerProvider ctxt, BeanProperty property) {
        JsonFormat.Value format;
        if (property != null) {
            format = property.findPropertyFormat(ctxt.getConfig(), handledType());
        } else {
            // even without property or AnnotationIntrospector, may have type-specific defaults
            format = ctxt.getDefaultPropertyFormat(handledType());
        }
        String pattern = format.getPattern();
        if (StringUtils.isNotBlank(pattern)) {
            return (JsonSerializer<T>) CACHE.computeIfAbsent(getClass(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(pattern, this::newInstance);
        }
        return this;
    }
}
