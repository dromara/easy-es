package org.dromara.easyes.common.utils.jackson.serializer;

import org.dromara.easyes.common.utils.DateUtil;

import java.util.Date;

public class DateSerializer extends AbstractJsonSerializer<Date, DateSerializer> {
    public DateSerializer() {
        super();
    }

    public DateSerializer(String pattern) {
        super(pattern);
    }

    @Override
    protected String format(Date date) {
        return DateUtil.format(date, pattern);
    }

    @Override
    protected DateSerializer newInstance(String pattern) {
        return new DateSerializer(pattern);
    }

}
