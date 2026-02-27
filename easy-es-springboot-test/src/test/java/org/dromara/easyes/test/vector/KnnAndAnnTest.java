package org.dromara.easyes.test.vector;

import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lzh
 * @date 25/09
 */
@SpringBootTest(classes = TestEasyEsApplication.class)
public class KnnAndAnnTest {
    @Resource
    private DocumentMapper documentMapper;

    private final static String testDataId = "lzh-2025";

    @Test
    public void testCreateVectorIndex() {
        documentMapper.createIndex();
    }

    @Test
    public void testInsertVectorData() {
        // 测试插入数据
        Document document = new Document();
        document.setEsId(testDataId);
        document.setContent("测试插入向量数据");
        document.setVectors(new double[]{0.39684247970581055, 0.7687071561813354, 0.5145490765571594});
        int successCount = documentMapper.insert(document);
        Assertions.assertEquals(1, successCount);
    }

    @Test
    public void testUpdateVectorData() {
        // 测试插入数据
        Document document = new Document();
        document.setEsId(testDataId);
        document.setContent("测试更新向量数据");
        document.setVectors(new double[]{0.39684247970581666, 0.768707156181666, 0.5145490765571666});
        int successCount = documentMapper.updateById(document);
        Assertions.assertEquals(1, successCount);
    }

    @Test
    public void testknn() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        float[] queryVector = {0.39684247970581055f, 0.7687071561813354f, 0.5145490765571594f};
        wrapper.knn(Document::getVectors, queryVector, 10);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testAnn() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        float[] queryVector = {0.39684247970581055f, 0.7687071561813354f, 0.5145490765571594f};
        wrapper.ann(Document::getVectors, queryVector, 10, 100);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
}
