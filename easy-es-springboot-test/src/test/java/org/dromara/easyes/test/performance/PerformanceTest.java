package org.dromara.easyes.test.performance;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.DEFAULT_SIZE;


/**
 * 性能测试
 * 测试机器:i7 8核16G 1.8GHZ
 *
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Disabled
@Slf4j
@SpringBootTest(classes = TestEasyEsApplication.class)
public class PerformanceTest {
    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private ElasticsearchClient client;

    @Test
    public void testSelectByEE() {
        StopWatch stopwatch = StopWatch.createStarted();
        // 测试时需将Document实体上加上注解@TableName("索引名")
        // 将查询索引指定为和通过ElasticsearchClient查询时一样的索引名称
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getTitle, "茶叶")
                .match(Document::getContent, "茶叶");
        List<Document> documents = documentMapper.selectList(wrapper);

        // 查询总条数
        Long count = documentMapper.selectCount(new LambdaEsQueryWrapper<>());
        stopwatch.stop();
        log.info("本次查询从:{}条数据中共命中:{}条数据,耗时总计:{}毫秒", count, documents.size(), stopwatch.getTime(TimeUnit.MILLISECONDS));
        // 本次查询从:5135条数据中共命中:15条数据,耗时总计:434毫秒 多次测试均值维持440毫秒左右
        // 对比下面直接使用ElasticsearchClient查询,查询仅慢10毫秒左右, 除去注释,节省了5倍代码,且查询越复杂,节省越多
    }

    @Test
    public void testSelectByElasticsearchClient() {
        // 构建查询条件
        StopWatch stopwatch = StopWatch.createStarted();
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index("kiplatform_library")
                .query(a -> a.bool(b -> b
                        .must(c -> c.match(d -> d.field("title").field("茶叶")))
                        .must(c -> c.match(d -> d.field("content").field("茶叶"))
                        )
                ))
                .highlight(a -> a
                        .fields("title", b -> b.preTags("<em>").postTags("</em>"))
                )
                .size(DEFAULT_SIZE);

        SearchResponse<Document> search = null;
        try {
            search = client.search(builder.build(), Document.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Document> documents = search.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
        SearchRequest countRequest = SearchRequest.of(a -> a.index("kiplatform_library"));
        SearchResponse<Document> countResponse = null;
        try {
            countResponse = client.search(countRequest, Document.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Long count = countResponse.hits().total().value();
        stopwatch.stop();
        log.info("本次查询从:{}条数据中共命中:{}条数据,耗时总计:{}毫秒", count, documents.size(), stopwatch.getTime(TimeUnit.MILLISECONDS));
        // 本次查询从:5135条数据中共命中:15条数据,耗时总计:428毫秒 多次测试 均值维持430毫秒左右
    }


    @Test
    public void testInsertByEE() {
        StopWatch stopwatch = StopWatch.createStarted();
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Document document = new Document();
            document.setTitle("测试新增性能" + i);
            document.setContent("测试新增性能内容" + i);
            documents.add(document);
        }
        documentMapper.insertBatch(documents);
        stopwatch.stop();
        log.info("本次共插入:{}条数据,耗时:{}毫秒", documents.size(), stopwatch.getTime(TimeUnit.MILLISECONDS));
        // 本次共插入:100条数据,耗时:343毫秒 多次测试稳定在340毫秒左右
        // 对比下面直接使用ElasticsearchClient 并无明显差异
    }

    @Test
    public void testInsertByElasticsearchClient() {
        StopWatch stopWatch = StopWatch.createStarted();
        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        int total = 100;
        for (int i = 0; i < total; i++) {
            Document document = new Document();
            document.setTitle("测试新增性能" + i);
            document.setContent("测试新增性能内容" + i);
            bulkRequest.operations(a -> a.index(b -> b.index("document").document(document)));
        }
        try {
            client.bulk(bulkRequest.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        log.info("本次共插入:{}条数据,耗时:{}毫秒", total, stopWatch.getTime(TimeUnit.MILLISECONDS));
        // 本次共插入:100条数据,耗时:342毫秒 多次测试稳定在340毫秒左右
    }

    @Test
    public void testUpdateByEE() {
        StopWatch stopWatch = StopWatch.createStarted();
        Document document = new Document();
        document.setEsId("PmF0SH8B0E2Rzy0qcFBz");
        document.setTitle("哈哈哈");
        document.setContent("嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿");
        documentMapper.updateById(document);
        stopWatch.stop();
        log.info("耗时:{}毫秒", stopWatch.getTime(TimeUnit.MILLISECONDS));
        // 多次测试平均耗时:330毫秒
        // 对比下面直接使用ElasticsearchClient 并无明显差异
    }

    @Test
    public void testUpdateByElasticsearchClient() {
        StopWatch stopWatch = StopWatch.createStarted();
        Document document = new Document();
        document.setTitle("哈哈哈");
        document.setContent("嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿");
        UpdateRequest<Document, Document> updateRequest = new UpdateRequest.Builder<Document, Document>()
                .index("document")
                .doc(document)
                .build();
        try {
            client.update(updateRequest, Document.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        log.info("耗时:{}毫秒", stopWatch.getTime(TimeUnit.MILLISECONDS));
        // 多次测试平均耗时:323毫秒
    }

    @Test
    public void testDeleteByEE() {
        StopWatch stopWatch = StopWatch.createStarted();
        documentMapper.deleteById("TWF0SH8B0E2Rzy0qcFBz");
        stopWatch.stop();
        log.info("耗时:{}毫秒", stopWatch.getTime(TimeUnit.MILLISECONDS));
        // 多次测试平均耗时耗时:131毫秒
        // 对比下面直接使用ElasticsearchClient 并无明显差异
    }

    @Test
    public void testDeleteByElasticsearchClient() {
        StopWatch stopWatch = StopWatch.createStarted();
        DeleteRequest deleteRequest = new DeleteRequest.Builder()
                .index("document")
                .id("TWF0SH8B0E2Rzy0qcFBz")
                .build();
        try {
            client.delete(deleteRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        log.info("耗时:{}毫秒", stopWatch.getTime(TimeUnit.MILLISECONDS));
        // 多次测试平均耗时耗时:130毫秒
    }
}
