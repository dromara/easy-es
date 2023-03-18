package cn.easyes.test.nested;

import cn.easyes.core.conditions.select.LambdaEsQueryWrapper;
import cn.easyes.core.toolkit.FieldUtils;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.entity.Faq;
import cn.easyes.test.entity.User;
import cn.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.geometry.Point;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
        faqs.add(new Faq("问题1", "回答1"));
        faqs.add(new Faq("问题2", "回答2"));

        Set<Faq> faqs1 = new HashSet<>();
        faqs1.add(new Faq("问题3", "回答3"));
        faqs1.add(new Faq("问题4", "回答4"));
        users.add(new User("用户1", 18, "12345", faqs));
        users.add(new User("用户2", 19, "123", faqs1));
        document.setUsers(users);
        int successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);

        document.setEsId("6");
        users.clear();
        faqs.clear();
        faqs1.clear();
        faqs.add(new Faq("question1", "answer1"));
        faqs.add(new Faq("question2", "answer2"));

        faqs1.add(new Faq("q3", "a3"));
        faqs1.add(new Faq("q4", "a4"));
        users.add(new User("user1", 8, "12345", faqs));
        users.add(new User("u2", 9, "54321", faqs1));
        document.setUsers(users);
        successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);
    }

    @Test
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
        wrapper1.eq(Document::getTitle,"老汉")
                .nested("users.faqs", w -> w.eq("users.faqs.answer", "a4")
                        .match("users.faqs.faq_name", "q4"))
                .nested("users", w -> w.between("users.age", 1, 30))
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
        wrapper3.nested("users.faqs",w->w.match("users.faqs.faq_name", "q3").or().match("users.faqs.faq_name","q4"));
        List<Document> documents3 = documentMapper.selectList(wrapper3);
        System.out.println(documents3);
    }

}
