package org.dromara.easyes.common.utils.jackson.serializer;

import org.dromara.easyes.common.utils.DateUtil;

import java.time.LocalDateTime;

public class LocalDateTimeSerializer extends AbstractJsonSerializer<LocalDateTime, LocalDateTimeSerializer> {
    public LocalDateTimeSerializer() {
        super();
    }

    public LocalDateTimeSerializer(String pattern) {
        super(pattern);
    }

    @Override
    protected String format(LocalDateTime date) {
        return DateUtil.format(date, pattern);
    }

    @Override
    protected LocalDateTimeSerializer newInstance(String pattern) {
        return new LocalDateTimeSerializer(pattern);
    }

}
