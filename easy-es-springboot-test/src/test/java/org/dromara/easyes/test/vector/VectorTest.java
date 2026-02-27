package org.dromara.easyes.test.vector;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 向量测试
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class VectorTest {
    @Resource
    private DocumentMapper documentMapper;


    @Test
    public void testVectorInsert() {
        // 测试插入数据
        Document document = new Document();
        document.setEsId("35f6fff8-1d3b-48b6-a765-028ec81b1437");
        document.setContent("测试插入数据");
        document.setVectors(new double[]{0.39684247970581055, 0.7687071561813354, 0.5145490765571594});
        int successCount = documentMapper.insert(document);
        Assertions.assertEquals(1, successCount);
    }

    @Test
    public void testVectorUpdate() {
        // 测试插入数据
        Document document = new Document();
        document.setEsId("35f6fff8-1d3b-48b6-a765-028ec81b1437");
        document.setContent("测试更新向量数据");
        document.setVectors(new double[]{0.39684247970581666, 0.768707156181666, 0.5145490765571666});
        int successCount = documentMapper.updateById(document);
        Assertions.assertEquals(1, successCount);
    }

    @Test
    public void testVectorSearch() {
        Query query = Query.of(a -> a.scriptScore(b -> b
                .query(QueryBuilders.matchAll().build()._toQuery())
                .script(d -> d.inline(e -> e
                        .lang("painless")
                        .params("vector", JsonData.of(new double[]{0.39684247970581055, 0.7687071561813354, 0.5145490765571594}))
                        .source("cosineSimilarity(params.vector, 'vector') + 1.0")
                ))
        ));

        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.setSearchBuilder(new SearchRequest.Builder().query(query));

        List<Document> Documents = documentMapper.selectList(wrapper);
        Assertions.assertFalse(Documents.isEmpty());
    }

}