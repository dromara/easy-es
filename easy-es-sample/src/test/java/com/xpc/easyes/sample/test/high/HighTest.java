package com.xpc.easyes.sample.test.high;

import com.xpc.easyes.core.common.PageInfo;
import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 高阶语法测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class HighTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testFilterField() {
        // 测试只查询指定字段
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老汉";
        wrapper.eq(Document::getTitle, title);
        wrapper.select(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
    }

    @Test
    public void testNotFilterField() {
        // 测试不查询指定字段 (推荐)
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老汉";
        wrapper.eq(Document::getTitle, title);
        wrapper.notSelect(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);

        // 另外一种与mp一致语法的Lambda写法
        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.select(Document.class, d -> !Objects.equals(d.getColumn(), "title"));
        Document document1 = documentMapper.selectOne(wrapper);
        System.out.println(document1);
    }

    @Test
    public void testSort() {
        // 测试排序 为了测试排序,我们在Document对象中新增了创建时间字段,更新了索引,并新增了两条数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getContent, "推");
        wrapper.select(Document::getTitle, Document::getGmtCreate);
        List<Document> before = documentMapper.selectList(wrapper);
        System.out.println("before:" + before);
        wrapper.orderByDesc(Document::getGmtCreate);
        List<Document> desc = documentMapper.selectList(wrapper);
        System.out.println("desc:" + desc);
    }

    @Test
    public void testGroupBy() throws IOException {
        // 测试聚合
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getContent, "推");
        wrapper.groupBy(Document::getCreator);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testMatch() {
        // 测试分词查询
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        wrapper.match(Document::getContent, keyword);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testWeight() throws IOException {
        // 测试权重
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        float contentBoost = 5.0f;
        wrapper.match(Document::getContent, keyword, contentBoost);
        String creator = "老汉";
        float creatorBoost = 2.0f;
        wrapper.eq(Document::getCreator, creator, creatorBoost);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testHighlight() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        wrapper.match(Document::getContent, keyword);
        wrapper.highLight(Document::getContent);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testPageQuery() {
        String creator = "老汉";
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getCreator, creator);
        PageInfo<Document> documentPageInfo = documentMapper.pageQuery(wrapper);
        System.out.println(documentPageInfo);
    }

}
