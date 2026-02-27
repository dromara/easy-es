package org.dromara.easyes.common.utils.jackson.serializer;

import org.dromara.easyes.common.utils.DateUtil;

import java.time.LocalDate;

public class LocalDateSerializer extends AbstractJsonSerializer<LocalDate, LocalDateSerializer> {
    public LocalDateSerializer() {
        super();
    }

    public LocalDateSerializer(String pattern) {
        super(pattern);
    }

    @Override
    protected String format(LocalDate date) {
        return DateUtil.format(date, pattern);
    }

    @Override
    protected LocalDateSerializer newInstance(String pattern) {
        return new LocalDateSerializer(pattern);
    }

}
