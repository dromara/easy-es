package cn.easyes.test.nested;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.toolkit.FieldUtils;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.entity.Faq;
import cn.easyes.test.entity.User;
import cn.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.geometry.Point;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class NestedTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testInsert() {
        // 测试插入数据
        Document document = new Document();
        document.setId("5");
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
        users.add(new User("用户1", 18, faqs));
        users.add(new User("用户2", 19, faqs1));
        document.setUsers(users);
        int successCount = documentMapper.insert(document);
        Assert.assertEquals(successCount, 1);
    }

    @Test
    public void testNestedMatch() {
        // 嵌套查询 查询内容匹配人才且嵌套数据中用户名匹配"用户"的数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "人才");
        // 其中嵌套类的字段名称获取我们提供了工具类FieldUtils.val帮助用户通过lambda函数式获取字段名称,当然如果不想用也可以直接传字符串
        wrapper.nestedMatch(Document::getUsers, FieldUtils.val(User::getUsername), "用户");
        wrapper.nestedMatch("users.faqs", "faq_name", "问题");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
}
