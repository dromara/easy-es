package org.dromara.easyes.test.performance;

import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private RestHighLevelClient client;

    @Test
    public void testSelectByEE() {
        StopWatch stopwatch = StopWatch.createStarted();
        // 测试时需将Document实体上加上注解@TableName("索引名")
        // 将查询索引指定为和通过RestHighLevelClient查询时一样的索引名称
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getTitle, "茶叶")
                .match(Document::getContent, "茶叶");
        List<Document> documents = documentMapper.selectList(wrapper);

        // 查询总条数
        Long count = documentMapper.selectCount(new LambdaEsQueryWrapper<>());
        stopwatch.stop();
        log.info("本次查询从:{}条数据中共命中:{}条数据,耗时总计:{}毫秒", count, documents.size(), stopwatch.getTime(TimeUnit.MILLISECONDS));
        // 本次查询从:5135条数据中共命中:15条数据,耗时总计:434毫秒 多次测试均值维持440毫秒左右
        // 对比下面直接使用RestHighLevelClient查询,查询仅慢10毫秒左右, 除去注释,节省了5倍代码,且查询越复杂,节省越多
    }

    @Test
    public void testSelectByRestHighLevelClient() {
        // 构建查询条件
        StopWatch stopwatch = StopWatch.createStarted();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", "茶叶"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("content", "茶叶"));
        builder.query(boolQueryBuilder);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        builder.highlighter(highlightBuilder);
        builder.size(DEFAULT_SIZE);
        SearchRequest request = new SearchRequest("kiplatform_library").source(builder);
        SearchResponse search = null;
        try {
            search = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Document> documents = Arrays.stream(search.getHits().getHits())
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), Document.class))
                .collect(Collectors.toList());

        SearchRequest countRequest = new SearchRequest("kiplatform_library");
        SearchResponse countResponse = null;
        try {
            countResponse = client.search(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Long count = countResponse.getHits().getTotalHits().value;
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
        // 对比下面直接使用RestHighLevelClient 并无明显差异
    }

    @Test
    public void testInsertByRestHighLevelClient() {
        StopWatch stopWatch = StopWatch.createStarted();
        BulkRequest bulkRequest = new BulkRequest();
        int total = 100;
        for (int i = 0; i < total; i++) {
            Document document = new Document();
            document.setTitle("测试新增性能" + i);
            document.setContent("测试新增性能内容" + i);
            IndexRequest indexRequest = new IndexRequest("document");
            indexRequest.source(JSON.toJSON(document), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
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
        // 对比下面直接使用RestHighLevelClient 并无明显差异
    }

    @Test
    public void testUpdateByRestHighLevelClient() {
        StopWatch stopWatch = StopWatch.createStarted();
        Document document = new Document();
        document.setTitle("哈哈哈");
        document.setContent("嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿嘿");
        UpdateRequest updateRequest = new UpdateRequest("document", "RWF0SH8B0E2Rzy0qcFBz");
        updateRequest.doc(JSON.toJSONString(document), XContentType.JSON);
        try {
            client.update(updateRequest, RequestOptions.DEFAULT);
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
        // 对比下面直接使用RestHighLevelClient 并无明显差异
    }

    @Test
    public void testDeleteByRestHighLevelClient() {
        StopWatch stopWatch = StopWatch.createStarted();
        DeleteRequest deleteRequest = new DeleteRequest("document", "RWF0SH8B0E2Rzy0qcFBz");
        try {
            client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        log.info("耗时:{}毫秒", stopWatch.getTime(TimeUnit.MILLISECONDS));
        // 多次测试平均耗时耗时:130毫秒
    }
}
