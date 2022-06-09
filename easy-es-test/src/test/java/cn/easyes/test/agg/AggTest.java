package cn.easyes.test.agg;

import cn.easyes.core.biz.PageInfo;
import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 聚合测试
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class AggTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testDistinct() {
        // 查询所有标题为老汉的文档,根据创建者去重,并分页返回
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .distinct(Document::getCreator);
        PageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 10);
        System.out.println(pageInfo);
    }

    @Test
    public void testAgg() {
        // 根据创建者聚合,聚合完在该桶中再次根据点赞数聚合
        // 注意:指定的多个聚合参数为链式聚合,就是第一个聚合参数聚合之后的结果,再根据第二个参数聚合,对应Pipeline聚合
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .groupBy(Document::getCreator)
                .max(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testAggNotPipeline() {
        // 对于下面两个字段,如果不想以pipeline管道聚合,各自聚合的结果在各自的桶中展示的话,我们也提供了支持
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 指定启用管道聚合为false
        wrapper.groupBy(false, Document::getCreator, Document::getTitle);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
}

