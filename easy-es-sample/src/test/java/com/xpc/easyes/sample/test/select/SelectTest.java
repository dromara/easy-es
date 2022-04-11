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
        wrapper.limit(1);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
        Assert.assertEquals(title, document.getTitle());
    }

    @Test
    public void testSelectById() {
        // 测试根据id查询
        String id = "OWELCIAB0E2Rzy0qtVNY";
        Document document = documentMapper.selectById(id);
        System.out.println(document);
    }

    @Test
    public void testSelectList() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老王";
        wrapper.eq(Document::getTitle, title);
        wrapper.select(Document::getCreator);
        wrapper.notSelect(Document::getContent, Document::getGmtCreate);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testSelectBatchIds() {
        List<String> ids = Arrays.asList("OmEQCIAB0E2Rzy0qHFNV", "UykMUX0BUP1SGucePGhx");
        List<Document> documents = documentMapper.selectBatchIds(ids);
        System.out.println(documents);
    }

    @Test
    public void testTrackTotalHits() {
        // 查询超过1w条时,trackTotalHits=true 会自动开启
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.limit(20000);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
    }
}
