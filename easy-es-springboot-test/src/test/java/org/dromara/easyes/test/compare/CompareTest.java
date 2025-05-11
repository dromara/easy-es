package org.dromara.easyes.test.compare;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 对比测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class CompareTest {
    @Resource
    private DocumentMapper documentMapper;
    @Autowired
    private ElasticsearchClient client;

    public static RestClientOptions options = new RestClientOptions(RequestOptions.DEFAULT);

    @Test
    public void testCompare() {
        // 需求:查询出文档标题为 "中国功夫"且作者为"老汉"的所有文档
        // 传统方式, 直接用ElasticsearchClient进行查询 需要11行代码,还不包含解析代码
        String indexName = "document";
        SearchRequest searchRequest = SearchRequest.of(a -> a
                .index(indexName)
                .query(QueryBuilders.bool()
                        .must(x -> x.term(b -> b.field("title").value("中国功夫")))
                        .must(x -> x.term(b -> b.field("creator").value("老汉")))
                        .build()._toQuery()
                )
        );
        try {
            SearchResponse<Document> searchResponse = client.withTransportOptions(options).search(searchRequest, Document.class);
            // 然后从searchResponse中通过各种方式解析出DocumentList 省略这些代码...
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 使用Easy-ES 仅需3行
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "中国功夫").eq(Document::getCreator, "老汉");
        List<Document> documents = documentMapper.selectList(wrapper);

        // 如果查询条件更为复杂,且包含高亮权重等高阶语法 采用传统方式代码量与直接用Easy-Es差距更为明显
    }
}
