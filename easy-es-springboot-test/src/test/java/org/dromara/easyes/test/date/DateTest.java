package org.dromara.easyes.test.date;

import org.apache.commons.lang3.time.DateUtils;
import org.dromara.easyes.common.utils.DateUtil;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.toolkit.EEDateUtils;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.DateEntity;
import org.dromara.easyes.test.mapper.DateTestMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.dromara.easyes.common.constants.BaseEsConstants.DEFAULT_DATE_TIME_FORMAT;
import static org.dromara.easyes.common.constants.BaseEsConstants.UTC_WITH_XXX_OFFSET_TIME_FORMAT;

@DisplayName("easy-es日期功能单元测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class DateTest {
    @Resource
    private DateTestMapper dateTestMapper;

    @Test
    @Order(0)
    void init() {
        dateTestMapper.deleteIndex("date_test");
        dateTestMapper.createIndex();
        Date date = new Date();
        LocalDateTime ldt = LocalDateTime.now();
        LocalDate localDate = LocalDate.now();
        ArrayList<DateEntity> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DateEntity cus = new DateEntity()
                    .setDate(date)
                    .setDate2(date)
                    .setLocalDate(localDate)
                    .setLdt(ldt)
                    .setLdt2(ldt);
            list.add(cus);
            date = DateUtils.addDays(date, 1);
            ldt = ldt.plusDays(1);
            localDate = localDate.plusDays(1);
        }
        dateTestMapper.insertBatch(list);
        dateTestMapper.refresh();
    }

    @Test
    @Order(1)
    void query() {
        Date date = new Date();
        LocalDateTime ldt = LocalDateTime.now();
        LocalDate localDate = LocalDate.now();
        ldt = ldt.plusDays(50);
        LambdaEsQueryWrapper<DateEntity> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.between(DateEntity::getLdt, EEDateUtils.format(DateEntity::getLdt, ldt),
                        EEDateUtils.format(DateEntity::getLdt, ldt.plusDays(30)))
                .between(DateEntity::getLdt2, EEDateUtils.format(DateEntity::getLdt2, ldt),
                        EEDateUtils.format(DateEntity::getLdt2, ldt.plusDays(20)))
                .gt(DateEntity::getDate, EEDateUtils.format(DateEntity::getDate, date))
                .gt(DateEntity::getDate2, EEDateUtils.format(DateEntity::getDate2, DateUtils.addDays(date, 60)))
                .lt(DateEntity::getLocalDate, EEDateUtils.format(DateEntity::getLocalDate, localDate.plusDays(70)));

        // 实际查询区间为60-70天，结果应该是9
        List<DateEntity> list = dateTestMapper.selectList(wrapper);
        System.out.println(list.size());
    }


    /**
     * 给出的时间字符串 "2025-05-31T16:00:00.000Z" 遵循 ISO 8601 标准格式。这种格式通常用于表示日期和时间，且易于人类阅读和机器解析。具体来说：
     * 2025 表示年份。
     * 05 表示月份（5月）。
     * 31 表示该月的第几天（31日）。
     * T 是日期和时间之间的分隔符，表示后面跟着的是时间部分。
     * 16:00:00 表示一天中的时间，分别为小时、分钟和秒（即下午4点整）。
     * .000 表示毫秒数，在这个例子中为0毫秒。
     * Z 表示该时间为 UTC 时间（零时区），即与格林尼治标准时间（GMT）相同。
     * 因此，这是一个符合 ISO 8601 标准的 UTC 时间戳，精确到毫秒。如果你需要将其转换为其他时区的时间，请相应地调整小时数，并考虑是否需要更改日期。
     * 例如，在中国标准时间（CST, UTC+8），这个时间会是 2025 年 6 月 1 日 00:00:00。
     */
    @Test
    void utilTest() {
        // 都是东八区的6月1日0点
        testParse("2025-05-31T16:00:00.000Z", UTC_WITH_XXX_OFFSET_TIME_FORMAT);
        testParse("2025-06-01T00:00:00.000+08:00", UTC_WITH_XXX_OFFSET_TIME_FORMAT);
        testParse("2025-05-31T22:00:00.000+06:00", UTC_WITH_XXX_OFFSET_TIME_FORMAT);
        testParse("2025-06-01 00:00:00", DEFAULT_DATE_TIME_FORMAT);
        testParse("2025-06-01T00:00:00.000", "yyyy-MM-dd'T'HH:mm:ss.SSS");
        testParse("2025-06-01 00:00", "yyyy-MM-dd HH:mm");
        testParse("2025060100", "yyyyMMddHH");
        testParse("2025/06/01", "yyyy/MM/dd");
        System.out.println("---------");

        Date date = DateUtil.parse("2025-06-01 00:00:00", DEFAULT_DATE_TIME_FORMAT);
        LocalDate localDate = DateUtil.parseLocalDate("2025-06-01 00:00:00", DEFAULT_DATE_TIME_FORMAT);
        LocalDateTime localDateTime = DateUtil.parseLocalDateTime("2025-06-01 00:00:00", DEFAULT_DATE_TIME_FORMAT);

        String format = DateUtil.format(date, UTC_WITH_XXX_OFFSET_TIME_FORMAT);
        String format1 = DateUtil.format(localDate, DEFAULT_DATE_TIME_FORMAT);
        String format2 = DateUtil.format(localDateTime, "yyyy/MM/dd HH/mm/ss");
        System.out.println(format);
        System.out.println(format1);
        System.out.println(format2);
    }

    private static void testParse(String time, String pattern) {
        Date date = DateUtil.parse(time, pattern);
        LocalDate localDate = DateUtil.parseLocalDate(time, pattern);
        LocalDateTime localDateTime = DateUtil.parseLocalDateTime(time, pattern);
        System.out.println(date);
        System.out.println(localDate);
        System.out.println(localDateTime);
    }


}
