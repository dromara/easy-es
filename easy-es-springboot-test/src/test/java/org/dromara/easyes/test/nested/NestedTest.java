package org.dromara.easyes.test.nested;


import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.entity.Faq;
import org.dromara.easyes.test.entity.User;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.geometry.Point;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 嵌套测试
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class NestedTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    @Order(0)
    public void testCreateIndex() {
        // 初始化创建索引,配置开启手动挡后执行
        final Boolean success = documentMapper.createIndex();
        Assertions.assertTrue(success);
    }

    @Test
    @Order(1)
    public void testInsert() {
        // 测试插入数据
        Document document = new Document();
        document.setEsId("5");
        document.setTitle("老汉");
        document.setContent("人才");
        document.setCreator("吃饭");
        document.setLocation("40.171975,116.587105");
        document.setGmtCreate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        document.setCustomField("俄罗斯方块");
        Point point = new Point(13.400544, 52.530286);
        document.setGeoLocation(point.toString());
        document.setStarNum(1);
        List<User> users = new ArrayList<>();
        Set<Faq> faqs = new HashSet<>();
        faqs.add(new Faq("q1", "回答1"));
        faqs.add(new Faq("q2", "回答2"));

        Set<Faq> faqs1 = new HashSet<>();
        faqs1.add(new Faq("q4", "回答3"));
        faqs1.add(new Faq("q3", "回答4"));
        users.add(new User("u1", 18, "12345", faqs));
        users.add(new User("u2", 19, "123", faqs1));
        document.setUsers(users);
        int successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);

        document.setEsId("6");
        users.clear();
        faqs.clear();
        faqs1.clear();
        faqs.add(new Faq("q1", "answer1"));
        faqs.add(new Faq("q2", "answer2"));

        faqs1.add(new Faq("q3", "a3"));
        faqs1.add(new Faq("q4", "a4"));
        users.add(new User("u3", 8, "12345", faqs));
        users.add(new User("u4", 9, "54321", faqs1));
        document.setUsers(users);
        successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);

        document.setEsId("7");
        users.clear();
        faqs.clear();
        faqs1.clear();

        users.add(new User("葡萄糖", 8, "12345", faqs));
        faqs.add(new Faq("口服溶液", "a4"));
        document.setUsers(users);
        successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);
    }

    @Test
    @Order(2)
    public void testNestedMatch() {
        // 嵌套查询 查询年龄等于18或8，且密码等于12345的数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.nested(FieldUtils.val(Document::getUsers), w ->
                w.in("users.age", 8)
                        .eq("users.password", "12345"));
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);

        // 嵌套类型中的字段获取可以用FieldUtils.val或直接传入字符串
        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.eq(Document::getTitle, "老汉")
                .nested("users.faqs", w -> w.eq("users.faqs.answer", "a4")
                        .match("users.faqs.faq_name", "q4"))
                .nested("users", w -> w.match("users.user_name", "u3"))
                .match(Document::getCreator, "吃饭");
        List<Document> documents1 = documentMapper.selectList(wrapper1);
        System.out.println(documents1);

        LambdaEsQueryWrapper<Document> wrapper2 = new LambdaEsQueryWrapper<>();
        wrapper2.nested("users", w -> w.in("users.age", 18))
                .or()
                .nested("users.faqs", w -> w.match("users.faqs.faq_name", "q3"));
        List<Document> documents2 = documentMapper.selectList(wrapper2);

        System.out.println(documents2);

        LambdaEsQueryWrapper<Document> wrapper3 = new LambdaEsQueryWrapper<>();
        wrapper3.nested("users.faqs", w -> w.match("users.faqs.faq_name", "q2").or().match("users.faqs.faq_name", "q4"));
        List<Document> documents3 = documentMapper.selectList(wrapper3);
        System.out.println(documents3);
    }

    @Test
    @Order(3)
    public void testNestedMatchHighlight() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.nested("users", w -> w.match("users.user_name.ik", "葡萄")
                        .or()
                        .match("users.user_name.py", "葡萄"))
                .nested("users.faqs", w -> w.match("users.faqs.faqName.ik", "kou")
                        .or()
                        .match("users.faqs.faqName.py", "kou"));
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

}
