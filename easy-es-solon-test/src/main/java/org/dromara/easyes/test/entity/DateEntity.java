package org.dromara.easyes.test.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.Settings;
import org.dromara.easyes.annotation.rely.IdType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.dromara.easyes.common.constants.BaseEsConstants.ES_DEFAULT_DATE_TIME_FORMAT;

/**
 *
 */
@Data
@Accessors(chain = true)
@Settings
@IndexName(value = "date_test")
public class DateEntity implements Serializable {
    @IndexId(type = IdType.NONE)
    private String id;

    @IndexField(dateFormat = ES_DEFAULT_DATE_TIME_FORMAT)
    private Date date;

    private Date date2;

    @IndexField(dateFormat = "yyyy-MM-dd")
    private LocalDate localDate;

    @IndexField(dateFormat = ES_DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime ldt;

    private LocalDateTime ldt2;

}
