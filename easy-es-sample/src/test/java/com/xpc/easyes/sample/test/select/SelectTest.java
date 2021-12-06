package com.xpc.easyes.sample.test.select;

import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 查询测试
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 查询测试
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class SelectTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testSelect() {
        // 测试根据条件查询
        String title = "老汉";
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, title);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
        Assert.assertEquals(title, document.getTitle());
    }

    @Test
    public void testSelectById() {
        // 测试根据id查询
        String id = "VSkMUX0BUP1SGucePGhx";
        Document document = documentMapper.selectById(id);
        System.out.println(document);
    }

    @Test
    public void testSelectList() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String creator = "老汉";
        wrapper.eq(Document::getCreator, creator);
        wrapper.select(Document::getTitle, Document::getCreator);
        wrapper.notSelect(Document::getTitle, Document::getContent);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testSelectBatchIds() {
        List<String> ids = Arrays.asList("VSkMUX0BUP1SGucePGhx", "UykMUX0BUP1SGucePGhx");
        List<Document> documents = documentMapper.selectBatchIds(ids);
        System.out.println(documents);
    }
}
