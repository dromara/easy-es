package org.dromara.easyes.test.other;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import org.dromara.easyes.common.exception.EasyEsException;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * index和docValues测试
 */
@DisplayName("index和docValues测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class IndexFalseTest {
    @Resource
    private DocumentMapper documentMapper;

    /**
     * index为false时，无法搜索
     */
    @Test
    void testIndexFalse() {
//        documentMapper.deleteIndex("easyes_document");
//        documentMapper.createIndex();
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getIndexFalse, "777");
        Exception exception = null;
        try {
            documentMapper.selectList(wrapper);
        } catch (ElasticsearchException e) {
            e.printStackTrace();
            System.out.println(e.response());
//            failed to create query: Cannot search on field [indexFalse] since it is not indexed
            exception = e;
        }
        Assertions.assertNotNull(exception);
    }

    /**
     * docValues为false时，无法聚合
     */
    @Test
    void testDocValueFalse() {
//        documentMapper.deleteIndex("easyes_document");
//        documentMapper.createIndex();
        Exception exception = null;
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.termsAggregation(Document::getDocFalse);
        try {
            documentMapper.search(wrapper);
        } catch (EasyEsException e) {
            e.printStackTrace();
            if (e.getCause() instanceof ElasticsearchException) {
//                这个异常描述貌似有问题？
//                Can't load fielddata on [docFalse] because fielddata is unsupported on fields of type [keyword]. Use doc values instead.
                System.out.println(((ElasticsearchException) e.getCause()).response());
            }
            exception = e;
        }
        Assertions.assertNotNull(exception);
    }

}