package org.dromara.easyes.common.utils.jackson.deserializer;

import org.dromara.easyes.common.utils.DateUtil;

import java.time.LocalDate;

public class LocalDateDeserializer extends AbstractJsonDeserializer<LocalDate, LocalDateDeserializer> {
    public LocalDateDeserializer() {
        super();
    }

    public LocalDateDeserializer(String pattern) {
        super(pattern);
    }

    @Override
    protected LocalDate parse(String str) {
        return DateUtil.parseLocalDate(str, pattern);
    }

    @Override
    protected LocalDateDeserializer newInstance(String pattern) {
        return new LocalDateDeserializer(pattern);
    }
}
