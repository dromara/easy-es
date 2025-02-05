package org.dromara.easyes.test.agg;

import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 聚合测试
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class AggTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testDistinct() {
        // 查询所有标题为老汉的文档,根据副标题去重,并分页返回
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .distinct(Document::getSubTitle);
        EsPageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 10);
        System.out.println(pageInfo);
    }

    @Test
    public void testAgg() {
        // 根据副标题聚合,聚合完在该桶中再次根据点赞数聚合
        // 注意:指定的多个聚合参数为链式聚合,就是第一个聚合参数聚合之后的结果,再根据第二个参数聚合,对应Pipeline聚合
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .groupBy(Document::getSubTitle)
                .max(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testManyAgg() {
        // 根据文档标题聚合,聚合完在该桶中再次根据点赞数聚合,求最大最小值
        // 注意:指定的多个聚合参数为链式聚合,就是第一个聚合参数聚合之后的结果,再根据第二个参数聚合,对应Pipeline聚合
        // 实现 select title, max(startNum), min(startNum) from table group by title的效果
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .groupBy(Document::getTitle)
                .max(Document::getStarNum)
                .min(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTermsFuncAgg() {
        // 根据标题和副标题聚合,聚合完在该桶中再次根据点赞数聚合
        // 注意:指定的多个聚合参数为链式聚合,就是第一个聚合参数聚合之后的结果,再根据第二个参数聚合,对应Pipeline聚合
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .termsAggregation(Document::getTitle, Document::getSubTitle)
                .avg(Document::getStarNum, Document::getScore)
                .min(true, Document::getStarNum, Document::getScore)
                .max(false, Document::getStarNum, Document::getScore)
                .sum(false, false, Document::getStarNum, Document::getScore);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testTermsStrAgg() {
        // 根据标题和副标题聚合,聚合完在该桶中再次根据点赞数聚合
        // 注意:指定的多个聚合参数为链式聚合,就是第一个聚合参数聚合之后的结果,再根据第二个参数聚合,对应Pipeline聚合
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .termsAggregation("title", "subTitle")
                .avg("starNum", "score")
                .min(true, "starNum", "score")
                .max(false, "starNum", "score")
                .sum(false, false, "starNum", "score");
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }


    @Test
    public void testAggNotPipeline() {
        // 对于下面两个字段,如果不想以pipeline管道聚合,各自聚合的结果在各自的桶中展示的话,我们也提供了支持
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 指定启用管道聚合为false
        wrapper.groupBy(false, Document::getTitle, Document::getSubTitle);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
}

