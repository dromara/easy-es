package com.xpc.easyes.sample.test.select;

import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 滚动查询测试
 *
 * @author tudou
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class SearchTest {
    @Resource
    private DocumentMapper documentMapper;
    //测试的总条数
    private static final int TOTAL_COUNT = 30;
    String indexName = Document.class.getSimpleName().toLowerCase();

    /**
     * 测试数据
     */
    @Test
    public void testInsertBatch() {
        List<Document> documentList = new ArrayList<>();
        for (int i = 0; i < TOTAL_COUNT; i++) {
            Document document = new Document();
            document.setId("scroll" + i);
            document.setTitle("老李");
            document.setStarNum(i);
            documentList.add(document);
        }
        int successCount = documentMapper.insertBatch(documentList);
        System.out.println(successCount);
        assertEquals(successCount, TOTAL_COUNT);
    }

    /**
     * 滚动查询
     *
     * @throws IOException io异常
     */
    @Test
    public void testScrollSearch() throws IOException {
        // 测试根据条件查询
        String title = "老李";
        int pageSize = 20;
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, title).from(0).size(pageSize);
        SearchSourceBuilder searchSourceBuilder = documentMapper.getSearchSourceBuilder(wrapper);
        SearchRequest searchRequest = new SearchRequest(indexName);
        // 此处的searchSourceBuilder由上面EE构建而来,我们继续对其追加排序参数
        searchSourceBuilder.sort("starNum", SortOrder.DESC);
        searchSourceBuilder.sort("_id", SortOrder.DESC);
        searchRequest.source(searchSourceBuilder);
        //这句代码就是设置游标查询和过期时间，1m就是1分钟之内scrollId有效
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));
        SearchRequest request = new SearchRequest(indexName);
        request.scroll(scroll);
        request.source(searchSourceBuilder);
        SearchResponse response = documentMapper.search(request, RequestOptions.DEFAULT);
        //System.out.println(JSON.toJSONString(response));
        SearchHits hits = response.getHits();
        String scrollId = response.getScrollId();

        SearchHit[] searchHits = hits.getHits();
        int totalResultCount = 0;
        int firstResultCount = searchHits.length;
        assertEquals(firstResultCount, pageSize);
        //什么时候结束查询？当查询结果数量小于第一次查询执行的 size时，结束查询。
        while (searchHits != null && searchHits.length > 0) {
            totalResultCount += searchHits.length;
            //这里面保存的是上一次的排序结果，对应的是上面searchSourceBuilder.sort的顺序
            Object[] sortValues = searchHits[0].getSortValues();
            //注意这里是searchAfter
            searchSourceBuilder.searchAfter(sortValues);
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            response = documentMapper.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            searchHits = response.getHits().getHits();
        }
        assertEquals(totalResultCount, TOTAL_COUNT);
        System.out.println("firstResultCount:" + firstResultCount + ",totalResultCount:" + totalResultCount);
    }

}
