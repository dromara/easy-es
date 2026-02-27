package org.dromara.easyes.test.date;

import org.apache.commons.lang3.time.DateUtils;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.toolkit.EEDateUtils;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.DateEntity;
import org.dromara.easyes.test.mapper.DateTestMapper;
import org.junit.jupiter.api.*;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DisplayName("easy-es日期功能单元测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SolonTest(classes = TestEasyEsApplication.class)
public class DateTest {
    @Inject
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
            date = DateUtils.addDays(date,1);
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
}
