package org.dromara.easyes.common.utils.jackson.deserializer;

import org.dromara.easyes.common.utils.DateUtil;

import java.util.Date;

public class DateDeserializer extends AbstractJsonDeserializer<Date, DateDeserializer> {
    public DateDeserializer() {
        super();
    }

    public DateDeserializer(String pattern) {
        super(pattern);
    }

    @Override
    protected Date parse(String str) {
        return DateUtil.parse(str, pattern);
    }

    @Override
    protected DateDeserializer newInstance(String pattern) {
        return new DateDeserializer(pattern);
    }

}