package cn.easyes.test.other;

import cn.easyes.common.constants.BaseEsConstants;
import cn.easyes.common.enums.FieldType;
import cn.easyes.core.conditions.LambdaEsIndexWrapper;
import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.toolkit.EntityInfoHelper;
import cn.easyes.core.toolkit.EsWrappers;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.geometry.Rectangle;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * fieldData 创建索引测试
 *
 * @author dys
 * @since 1.0
 */
@DisplayName("fieldData测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class FieldDataTest {
    @Resource
    private DocumentMapper documentMapper;

    /**
     * 测试自动创建索引并插入数据
     */
    @Test
    @Order(1)
    void testAutoCreateIndex() {
        // 测试插入数据
        Document document = new Document();
        document.setEsId("1");
        document.setTitle("测试文档1");
        document.setContent("测试内容1");
        document.setCreator("老汉1");
        document.setLocation("40.171975,116.587105");
        document.setGmtCreate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        document.setCustomField("自定义字段1");
        document.setNullField("id为1的数据不是null,除此之外其它都是");
        Rectangle rectangle = new Rectangle(39.084509D, 41.187328D, 70.610461D, 20.498353D);
        document.setGeoLocation(rectangle.toString());
        document.setStarNum(1);
        document.setFiledData("123");
        int successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);
        LambdaEsQueryWrapper<Document> lambdaEsQueryWrapper = EsWrappers.lambdaQuery(Document.class)
                .matchAllQuery()
                // 设置为如果filedData为false 或不设置 text类型排序会报错
                .orderByDesc(Document::getFiledData);
        List<Document> documents = documentMapper.selectList(lambdaEsQueryWrapper);
        Assertions.assertEquals(documents.size(), 1);
    }

    /**
     * 测试手动创建索引配置是否生效
     */
    @Test
    @Order(2)
    void testFiledData() {
        LambdaEsIndexWrapper<Document> wrapper = EsWrappers.lambdaIndex(Document.class)
                .indexName("document1")
                .mapping(Document::getTitle, FieldType.TEXT, true);
        Boolean index = documentMapper.createIndex(wrapper);
        Assertions.assertTrue(index);
    }

    @Test
    @Order(3)
    public void testDeleteIndex() {
        Boolean dcoument1 = documentMapper.deleteIndex("document1");
        boolean deleted = documentMapper.deleteIndex(EntityInfoHelper.getEntityInfo(Document.class).getIndexName());
        boolean lockDeleted = documentMapper.deleteIndex(BaseEsConstants.LOCK_INDEX);
        Assertions.assertTrue(dcoument1);
        Assertions.assertTrue(deleted);
        Assertions.assertTrue(lockDeleted);
    }
}