package org.dromara.easyes.common.utils.jackson.deserializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.dromara.easyes.common.utils.ExceptionUtils;
import org.dromara.easyes.common.utils.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.dromara.easyes.common.constants.BaseEsConstants.ES_DEFAULT_DATE_TIME_FORMAT;
import static org.dromara.easyes.common.constants.BaseEsConstants.UTC_WITH_XXX_OFFSET_TIME_FORMAT;
import static org.dromara.easyes.common.constants.BaseEsConstants.DEFAULT_DATE_TIME_FORMAT;

public abstract class AbstractJsonDeserializer<T, D extends AbstractJsonDeserializer<T, D>> extends JsonDeserializer<T> implements ContextualDeserializer {
    private static final Map<Class<?>, Map<String, AbstractJsonDeserializer<?, ?>>> CACHE = new ConcurrentHashMap<>();
    protected final String pattern;

    public AbstractJsonDeserializer() {
        this(DEFAULT_DATE_TIME_FORMAT);
    }

    public AbstractJsonDeserializer(String pattern) {
        if (ES_DEFAULT_DATE_TIME_FORMAT.equals(pattern)) {
            pattern = UTC_WITH_XXX_OFFSET_TIME_FORMAT;
        }
        this.pattern = pattern;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String str = p.getValueAsString();
        try {
            return parse(str);
        } catch (Exception e) {
            throw ExceptionUtils.eee("deserializer date fail：%s", e, str);
        }
    }

    abstract protected T parse(String str);

    abstract protected D newInstance(String pattern);

    @Override
    @SuppressWarnings("unchecked")
    public JsonDeserializer<T> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JsonFormat.Value format;
        if (property != null) {
            format = property.findPropertyFormat(ctxt.getConfig(), handledType());
        } else {
            // even without property or AnnotationIntrospector, may have type-specific defaults
            format = ctxt.getDefaultPropertyFormat(handledType());
        }
        String pattern = format.getPattern();
        if (StringUtils.isNotBlank(pattern)) {
            return (JsonDeserializer<T>) CACHE.computeIfAbsent(getClass(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(pattern, this::newInstance);
        }
        return this;
    }
}
