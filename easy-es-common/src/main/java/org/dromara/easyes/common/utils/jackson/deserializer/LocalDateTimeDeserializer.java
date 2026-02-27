package org.dromara.easyes.common.utils.jackson.deserializer;

import org.dromara.easyes.common.utils.DateUtil;

import java.time.LocalDateTime;

public class LocalDateTimeDeserializer extends AbstractJsonDeserializer<LocalDateTime, LocalDateTimeDeserializer> {
    public LocalDateTimeDeserializer(String pattern) {
        super(pattern);
    }

    public LocalDateTimeDeserializer() {
        super();
    }

    @Override
    protected LocalDateTime parse(String str) {
        return DateUtil.parseLocalDateTime(str, pattern);
    }

    @Override
    protected LocalDateTimeDeserializer newInstance(String pattern) {
        return new LocalDateTimeDeserializer(pattern);
    }
}