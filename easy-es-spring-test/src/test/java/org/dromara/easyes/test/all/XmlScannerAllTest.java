package org.dromara.easyes.test.all;

import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.core.biz.EntityInfo;
import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.biz.OrderByParam;
import org.dromara.easyes.core.biz.SAPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.dromara.easyes.core.kernel.EsWrappers;
import org.dromara.easyes.core.kernel.WrapperProcessor;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.geometry.Circle;
import org.elasticsearch.geometry.Point;
import org.elasticsearch.geometry.Rectangle;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.dromara.easyes.common.constants.BaseEsConstants.KEYWORD_SUFFIX;

/**
 * @author MoJie
 * @since 2.0
 */
@DisplayName("easy-es核心功能单元测试(xml方式)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
@SpringJUnitConfig(locations = {"classpath:spring-context.xml"})
public class XmlScannerAllTest {

    @Resource
    private DocumentMapper documentMapper;

    @Test
    @Order(0)
    public void testCreateIndex() {
        // 0.前置操作 创建索引 需确保索引托管模式处于manual手动挡,若为自动挡则会冲突.
        boolean success = documentMapper.createIndex();
        Assertions.assertTrue(success);
    }

    // 1.新增
    @Test
    @Order(1)
    public void testInsert() {
        // 测试插入数据
        Document document = new Document();
        document.setEsId("1");
        document.setCaseTest("Test");
        document.setTitle("测试文档1");
        document.setContent("测试内容1我是大家发达酸辣粉家里都是测试阿拉拉肥发的是就测试时风刀霜剑阿凯刘非打死了交付率及时点法律就反倒是测测试初拉力肌肥大来极氪吉利发送代理费逻辑逻辑发骚鸡,测试发力了哦哦我");
        document.setCreator("老汉1");
        document.setIpAddress("192.168.1.1");
        document.setLocation("40.171975,116.587105");
        document.setGmtCreate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        document.setCustomField("自定义字段1");
        document.setNullField("id为1的数据不是null,除此之外其它都是");
        Rectangle rectangle = new Rectangle(39.084509D, 41.187328D, 70.610461D, 20.498353D);
        document.setGeoLocation(rectangle.toString());
        document.setStarNum(1);
        document.setMultiField("葡萄糖酸钙口服溶液");
        document.setEnglish("Calcium Gluconate");
        document.setBigNum(new BigDecimal("66.66"));
        document.setVector(new double[]{0.39684247970581666, 0.768707156181666, 0.5145490765571666});
//        System.out.println(JsonUtils.toJsonPrettyStr(document));
        int successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);
    }

    @Test
    @Order(2)
    public void testBatchInsert() {
        List<Document> documentList = new ArrayList<>();
        for (int i = 2; i < 23; i++) {
            Document document = new Document();
            document.setEsId(Integer.toString(i));
            document.setTitle("测试文档" + i);
            document.setContent("测试内容" + i);
            document.setCreator("老汉" + i);
            document.setGmtCreate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            document.setCustomField("自定义字段" + i);
            Point point = new Point(13.400544 + i, 52.530286 + i);
            document.setGeoLocation(point.toString());
            document.setStarNum(i);
            document.setVector(new double[]{35.89684247970581666, 86.268707156181666, 133.1145490765571666});
            // 针对个别数据 造一些差异项 方便测试不同场景
            if (i == 2) {
                document.setLocation("40.17836693398477,116.64002551005981");
                document.setStarNum(1);
            } else if (i == 3) {
                document.setLocation("40.19103839805197,116.5624013764374");
            } else if (i == 4) {
                document.setLocation("40.13933715136454,116.63441990026217");
            }
            documentList.add(document);
        }
        int count = documentMapper.insertBatch(documentList);
        Assertions.assertEquals(documentList.size(), count);
    }

    // 2.修改
    @Test
    @Order(3)
    public void testUpdateById() {
        Document document = new Document();
        document.setEsId("1");
        document.setTitle("测试文档1标题被更新了");
        int count = documentMapper.updateById(document);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(4)
    public void testUpdateByWrapper() {
        LambdaEsUpdateWrapper<Document> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.eq(Document::getTitle, "测试文档2");
        wrapper.set(Document::getContent, "测试文档内容2的内容被更新了");

        int count = documentMapper.update(wrapper);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(5)
    public void testUpdateByChainWrapper() {
        int count = EsWrappers.lambdaChainUpdate(documentMapper)
                .eq(Document::getTitle, "测试文档3")
                .set(Document::getContent, "测试文档内容3的内容被修改了")
                .update();
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(6)
    public void testUpdateBySetSearchSourceBuilder() {
        LambdaEsUpdateWrapper<Document> wrapper = new LambdaEsUpdateWrapper<>();
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
        searchBuilder.query(QueryBuilders.term()
                .field(FieldUtils.val(Document::getTitle) + KEYWORD_SUFFIX)
                .value("测试文档2")
                .build()._toQuery()
        );
        wrapper.setSearchBuilder(searchBuilder);

        Document document = new Document();
        document.setContent("测试文档内容2的内容再次被更新了");
        int count = documentMapper.update(document, wrapper);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(7)
    public void testUpdateByWrapperAndEntity() {
        LambdaEsUpdateWrapper<Document> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        Document document = new Document();
        document.setCustomField("被更新的自定义字段");
        int count = documentMapper.update(document, wrapper);
        Assertions.assertEquals(22, count);
    }

    // 3.查询

    @Test
    @Order(8)
    public void testSQL() {
        // 注意 sql中的from后面跟的是要被查询的索引名,也可以是索引别名(效果一样) 由于索引名可能会变,所以此处我采用别名ee_default_alias进行查询
        String sql = "select count(*) from ee_default_alias where star_num > 0";
        String jsonResult = documentMapper.executeSQL(sql);
        System.out.println(jsonResult);
        Assertions.assertNotNull(jsonResult);
    }

    @Test
    @Order(9)
    public void testDSL() {
        String dsl = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"title.keyword\":{\"value\":\"测试文档3\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"track_total_hits\":2147483647,\"highlight\":{\"pre_tags\":[\"<em>\"],\"post_tags\":[\"</em>\"],\"fragment_size\":2,\"fields\":{\"content\":{\"type\":\"unified\"}}}}";
        String jsonResult = documentMapper.executeDSL(dsl);
        System.out.println(jsonResult);
        Assertions.assertNotNull(jsonResult);
    }

    @Test
    @Order(10)
    public void testSelectOne() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "内容")
                .orderByAsc(Document::getStarNum, Document::getEsId)
                .limit(1);
        Document document = documentMapper.selectOne(wrapper);
        Assertions.assertEquals("测试文档1标题被更新了", document.getTitle());
    }

    @Test
    @Order(11)
    public void testOne() {
        // 链式调用
        Document document = EsWrappers.lambdaChainQuery(documentMapper).eq(Document::getTitle, "测试文档3").one();
        Assertions.assertEquals(document.getContent(), "测试文档内容3的内容被修改了");
    }

    @Test
    @Order(12)
    public void testDefaultMethod() {
        List<Document> documents = documentMapper.testDefaultMethod();
        Assertions.assertEquals(documents.size(), 1);
    }

    @Test
    @Order(13)
    public void testSelectById() {
        Document document = documentMapper.selectById(1);
        Assertions.assertEquals("1", document.getEsId());
        Assertions.assertEquals("老汉1", document.getCreator());
    }

    @Test
    @Order(14)
    public void testSelectBatchIds() {
        List<Document> documents = documentMapper.selectBatchIds(Arrays.asList("1", "2"));
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(15)
    public void testSelectList() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCustomField, "字段");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(16)
    public void testIgnoreCase() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getCaseTest, "test");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(17)
    public void testIp() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getIpAddress, "192.168.0.0/16");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(documents.size(), 1);
    }

    @Test
    @Order(18)
    public void testSelectCount() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCustomField, "字段");
        Long count = documentMapper.selectCount(wrapper);
        Assertions.assertEquals(22L, count);
    }

    @Test
    @Order(19)
    public void testSelectCountDistinct() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCustomField, "字段");
        wrapper.distinct(Document::getStarNum);
        Long count = documentMapper.selectCount(wrapper, true);
        Assertions.assertEquals(21L, count);
    }

    @Test
    @Order(20)
    public void testConditionAllEq() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        Map<String, Object> map = new HashMap<>();
        map.put("title", "测试文档3");
        map.put("creator.keyword", "老汉3");
        map.put("starNum", 3);
        wrapper.allEq(map);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
        Assertions.assertEquals("测试文档3", documents.get(0).getTitle());
    }

    @Test
    @Order(21)
    public void testConditionEq() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "测试文档10");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
        Assertions.assertEquals("测试文档10", documents.get(0).getTitle());
    }

    @Test
    @Order(22)
    public void testConditionGt() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.gt(Document::getStarNum, 20);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(23)
    public void testConditionGe() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.ge(Document::getStarNum, 20);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(3, documents.size());
    }

    @Test
    @Order(24)
    public void testConditionLt() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.lt(Document::getStarNum, 3);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(25)
    public void testConditionLe() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.le(Document::getStarNum, 3);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(3, documents.size());
    }

    @Test
    @Order(26)
    public void testConditionBetween() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.between(Document::getStarNum, 1, 10);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(10, documents.size());
    }

    @Test
    @Order(27)
    public void testConditionLike() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.like(Document::getTitle, "试文档");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(28)
    public void testConditionLikeLeft() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeLeft(Document::getTitle, "文档10");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(29)
    public void testConditionLikeRight() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getTitle, "测试文");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(30)
    public void testConditionIsNotNull() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.isNotNull(Document::getNullField);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(31)
    public void testConditionExists() {
        // exists等价于isNotNull 在es中更推荐此种语法
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.exists(Document::getNullField);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(32)
    public void testConditionIn() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.in(Document::getEsId, "1", "2", "3");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(3, documents.size());

        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.in(Document::getStarNum, 7, 8);
        List<Document> documents1 = documentMapper.selectList(wrapper1);
        Assertions.assertEquals(2, documents1.size());
    }

    @Test
    @Order(33)
    public void testConditionGroupBy() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .groupBy(Document::getStarNum);
        SearchResponse<Document> response = documentMapper.search(wrapper);
        Aggregate aggregate = response.aggregations().get("starNumTerms");
        LongTermsBucket bucket = aggregate.lterms().buckets().array().get(0);
        Assertions.assertTrue(bucket.key().equals("1") && bucket.docCount() == 2L);
    }

    @Test
    @Order(34)
    public void testConditionMax() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .max(Document::getStarNum);
        SearchResponse<Document> response = documentMapper.search(wrapper);
        Aggregate agg = response.aggregations().get("starNumMax");
        Assertions.assertTrue(agg.max().value() > 21);
    }

    @Test
    @Order(35)
    public void testConditionMin() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .min(Document::getStarNum);
        SearchResponse<Document> response = documentMapper.search(wrapper);
        double parsedMin = response.aggregations().get("starNumMin").min().value();
        Assertions.assertTrue(parsedMin > 0 && parsedMin < 2);
    }

    @Test
    @Order(36)
    public void testConditionSum() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .sum(Document::getStarNum);
        SearchResponse<Document> response = documentMapper.search(wrapper);
        double parsedSum = response.aggregations().get("starNumSum").sum().value();
        Assertions.assertTrue(parsedSum >= 252);
    }

    @Test
    @Order(37)
    public void testConditionAvg() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .avg(Document::getStarNum);
        SearchResponse<Document> response = documentMapper.search(wrapper);
        double parsedAvg = response.aggregations().get("starNumAvg").avg().value();
        Assertions.assertTrue(parsedAvg > 11 && parsedAvg < 12);
    }

    @Test
    @Order(38)
    public void testConditionDistinct() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .distinct(Document::getStarNum);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(21, documents.size());
    }

    @Test
    @Order(39)
    public void testConditionLimit() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉")
                .limit(2, 5);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(5, documents.size());
    }

    @Test
    @Order(40)
    public void testConditionFromAndSize() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉")
                .from(20)
                .size(2);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(41)
    public void testConditionIndex() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉")
                .index(EntityInfoHelper.getEntityInfo(Document.class).getIndexName());
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(42)
    public void testSetSearchSourceBuilder() {
        EntityInfo e = EntityInfoHelper.getEntityInfo(Document.class);
        // 测试混合查询的另一种方式
        SearchRequest.Builder searchSourceBuilder = new SearchRequest.Builder()
                .query(QueryBuilders.match()
                        .field(FieldUtils.val(Document::getCreator))
                        .query("老汉")
                        .build()._toQuery()
                )
                .size(e.getMaxResultWindow().intValue());
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.setSearchBuilder(searchSourceBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(43)
    public void testConditionAnd() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.in(Document::getStarNum, 1, 2, 3, 4, 10, 11)
                .and(w -> w.eq(Document::getTitle, "测试文档10").match(Document::getCreator, "老汉"));
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(44)
    public void testConditionOr() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.in(Document::getStarNum, 1, 10, 12, 13)
                .or(i -> i.eq(Document::getTitle, "测试文档11").eq(Document::getTitle, "测试文档10"));
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(5, documents.size());
    }

    @Test
    @Order(45)
    public void testConditionOrInner() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "测试文档10")
                .or()
                .eq(Document::getTitle, "测试文档20");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(46)
    public void testConditionFilter() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getStarNum, 10)
                .filter().eq(Document::getTitle, "测试文档10");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(47)
    public void testConditionNot() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.in(Document::getStarNum, 10, 11, 12, 13)
                .and(i -> i.not().eq(Document::getTitle, "测试文档10").not().eq(Document::getTitle, "测试文档11"));
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(48)
    public void testPageQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        EsPageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 5);
        Assertions.assertEquals(5, pageInfo.getSize());
        Assertions.assertEquals(22, pageInfo.getTotal());
    }

    @Test
    @Order(49)
    public void testChainPage() {
        // 链式
        EsPageInfo<Document> pageInfo = EsWrappers.lambdaChainQuery(documentMapper)
                .match(Document::getCreator, "老汉")
                .page(1, 5);
        Assertions.assertEquals(5, pageInfo.getSize());
        Assertions.assertEquals(22, pageInfo.getTotal());
    }

    @Test
    @Order(50)
    public void testSearchAfter() {
        LambdaEsQueryWrapper<Document> lambdaEsQueryWrapper = EsWrappers.lambdaQuery(Document.class);
        lambdaEsQueryWrapper.size(10);
        lambdaEsQueryWrapper.orderByDesc(Document::getEsId, Document::getStarNum);
        SAPageInfo<Document> saPageInfo = documentMapper.searchAfterPage(lambdaEsQueryWrapper, null, 10);
        //第一页
        System.out.println(saPageInfo);
        Assertions.assertEquals(10, saPageInfo.getList().size());

        //获取下一页
        List<FieldValue> nextSearchAfter = saPageInfo.getNextSearchAfter();
        SAPageInfo<Document> next = documentMapper.searchAfterPage(lambdaEsQueryWrapper, nextSearchAfter, 10);
        Assertions.assertEquals(10, next.getList().size());
    }

    @Test
    @Order(51)
    public void testFilterField() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "测试文档10")
                .select(Document::getEsId, Document::getContent);
        Document document = documentMapper.selectOne(wrapper);
        Assertions.assertNotNull(document.getContent());
        Assertions.assertNotNull(document.getEsId());
        Assertions.assertNull(document.getTitle());
    }

    @Test
    @Order(52)
    public void testNotFilterField() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "测试文档10")
                .notSelect(Document::getEsId, Document::getContent);
        Document document = documentMapper.selectOne(wrapper);
        Assertions.assertNull(document.getContent());
        Assertions.assertNull(document.getEsId());
        Assertions.assertNotNull(document.getTitle());
    }

    @Test
    @Order(53)
    public void testOrderByDesc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        wrapper.orderByDesc(Document::getStarNum);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("22", documents.get(0).getEsId());
        Assertions.assertEquals("21", documents.get(1).getEsId());
    }

    @Test
    @Order(54)
    public void testOrderByAsc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        wrapper.orderByAsc(Document::getStarNum, Document::getEsId);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("1", documents.get(0).getEsId());
        Assertions.assertEquals("22", documents.get(21).getEsId());
    }

    @Test
    @Order(55)
    public void testOrderBy() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        List<OrderByParam> orderByParams = new ArrayList<>();
        OrderByParam orderByParam = new OrderByParam();
        orderByParam.setOrder("star_num");
        orderByParam.setSort("DESC");
        orderByParams.add(orderByParam);
        wrapper.orderBy(orderByParams);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("22", documents.get(0).getEsId());
        Assertions.assertEquals("21", documents.get(1).getEsId());
    }

    @Test
    @Order(56)
    public void testOrderByDistanceAsc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoLocation centerPoint = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(41.0).lon(116.0)));
        wrapper.match(Document::getCreator, "老汉")
                .geoDistance(Document::getLocation, 168.8, centerPoint)
                .orderByDistanceAsc(Document::getLocation, centerPoint);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("3", documents.get(0).getEsId());
        Assertions.assertEquals("4", documents.get(3).getEsId());
    }

    @Test
    @Order(57)
    public void testOrderByDistanceDesc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoLocation centerPoint = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(41.0).lon(116.0)));
        wrapper.match(Document::getCreator, "老汉")
                .geoDistance(Document::getLocation, 168.8, centerPoint)
                .orderByDistanceDesc(Document::getLocation, centerPoint);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("4", documents.get(0).getEsId());
        Assertions.assertEquals("3", documents.get(3).getEsId());
    }

    @Test
    @Order(58)
    public void testOrderByDistanceMulti() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoLocation centerPoint = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(41.0).lon(116.0)));
        GeoLocation centerPoint1 = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(42.0).lon(118.0)));
        wrapper.match(Document::getCreator, "老汉")
                .geoDistance(Document::getLocation, 168.8, centerPoint)
                .orderByDistanceDesc(Document::getLocation, centerPoint)
                .orderByDistanceDesc(Document::getLocation, centerPoint1);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("4", documents.get(0).getEsId());
        Assertions.assertEquals("3", documents.get(3).getEsId());
    }

    @Test
    @Order(59)
    public void testSortByScore() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉11");
        wrapper.sortByScore();
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("11", documents.get(0).getEsId());
    }

    @Test
    @Order(60)
    public void testSort() {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(Document.class);
        String realField = FieldUtils.getRealFieldAndSuffix(
                FieldUtils.val(Document::getStarNum),
                entityInfo.getMappingColumnMap(),
                entityInfo);

        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        wrapper.sort(SortOptions.of(a -> a.field(b -> b
                .field(realField)
                .order(SortOrder.Desc)
        )));
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("22", documents.get(0).getEsId());
        Assertions.assertEquals("21", documents.get(1).getEsId());
    }

    @Test
    @Order(61)
    public void testMatch() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(62)
    public void testMatchPhrase() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrase(Document::getContent, "测试");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertFalse(documents.isEmpty());

        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.matchPhrase(Document::getContent, "内容测试");
        List<Document> documents1 = documentMapper.selectList(wrapper1);
        Assertions.assertTrue(documents1.size() <= 0);
    }

    @Test
    @Order(63)
    public void testMatchAllQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchAllQuery();
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(64)
    public void testMatchPhrasePrefixQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrasePrefixQuery(Document::getContent, "测试");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(65)
    public void testMultiMatchQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.multiMatchQuery("老汉", Document::getContent, Document::getCreator, Document::getCustomField);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());

        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.multiMatchQuery("更新", Document::getContent, Document::getCreator);

        List<Document> documents1 = documentMapper.selectList(wrapper1);
        Assertions.assertEquals(1, documents1.size());
    }

    @Test
    @Order(66)
    public void testQueryStringQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.queryStringQuery("老汉");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(67)
    public void testPrefixQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.prefixQuery(Document::getContent, "测试");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(68)
    public void testHighLight() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .match(Document::getCustomField, "字段");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertTrue(documents.get(0).getHighlightContent().contains("测试"));
    }

    @Test
    @Order(69)
    public void testGeoBoundingBox() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoLocation leftTop = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(41.187328D).lon(115.498353D)));
        GeoLocation bottomRight = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(39.084509D).lon(117.610461D)));
        wrapper.geoBoundingBox(Document::getLocation, leftTop, bottomRight);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(4, documents.size());
    }

    @Test
    @Order(70)
    public void testGeoDistance() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoLocation geoPoint = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(41.0).lon(116.0)));
        wrapper.geoDistance(Document::getLocation, 168.8, DistanceUnit.Kilometers, geoPoint);
        wrapper.sort(SortOptions.of(a -> a.geoDistance(b -> b
                .field(FieldUtils.val(Document::getLocation))
                .location(geoPoint)
                .unit(DistanceUnit.Kilometers)
                .distanceType(GeoDistanceType.Arc)
                .order(SortOrder.Desc)
        )));
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(4, documents.size());
    }

    @Test
    @Order(71)
    public void testGeoPolygon() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        List<GeoLocation> geoPoints = new ArrayList<>();
        GeoLocation geoPoint = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(40.178012).lon(116.577188)));
        GeoLocation geoPoint1 = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(40.169329).lon(116.586315)));
        GeoLocation geoPoint2 = GeoLocation.of(a -> a.latlon(a1 -> a1.lat(40.178288).lon(116.591813)));
        geoPoints.add(geoPoint);
        geoPoints.add(geoPoint1);
        geoPoints.add(geoPoint2);
        wrapper.geoPolygon(Document::getLocation, geoPoints);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(72)
    public void testGeoShape() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        Circle circle = new Circle(13, 14, 100);
        wrapper.geoShape(Document::getGeoLocation, circle, GeoShapeRelation.Disjoint);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(73)
    public void testMultiFieldSelect() {
        // 药品 中文名叫葡萄糖酸钙口服溶液 英文名叫 Calcium Gluconate 汉语拼音为 putaotangsuangaikoufurongye
        // 用户可以通过模糊检索,例如输入 Calcium 或 葡萄糖 或 putaotang时对应药品均可以被检索到
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match("english", "Calcium")
                .or()
                .match("multi_field.zh", "葡萄糖")
                .or()
                .match("multi_field.pinyin", "putaotang");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    @Order(74)
    public void testVector() {
        // 向量查询, 查询条件构造
        Query query = Query.of(a -> a.scriptScore(b -> b
                .query(QueryBuilders.matchAll().build()._toQuery())
                .script(d -> d.inline(e -> e
                        .lang("painless")
                        .params("vector", JsonData.of(new double[]{0.39684247970581055, 0.7687071561813354, 0.5145490765571594}))
                        .source("cosineSimilarity(params.vector, 'vector') + 1.0")
                ))
        ));
        SearchRequest.Builder searchSourceBuilder = new SearchRequest.Builder();
        searchSourceBuilder.query(query);
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.setSearchBuilder(searchSourceBuilder);

        List<Document> Documents = documentMapper.selectList(wrapper);
        Assertions.assertFalse(Documents.isEmpty());
    }

    @Test
    @Order(75)
    public void testSetRequestOptions() {
        // 可设置自定义请求参数,覆盖默认配置, 解决报错 entity content is too long [168583249] for the configured buffer limit [104857600]
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(4 * 104857600));
        Boolean success = documentMapper.setRequestOptions(new RestClientOptions(builder.build()));
        Assertions.assertTrue(success);
    }

    // 4.删除
    @Test
    @Order(76)
    public void testDeleteById() {
        int count = documentMapper.deleteById("1");
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(77)
    public void testDeleteBatchIds() {
        List<String> idList = Arrays.asList("2", "3", "4");
        int count = documentMapper.deleteBatchIds(idList);
        Assertions.assertEquals(3, count);
    }

    @Test
    @Order(78)
    public void testDeleteByWrapper() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");

        int count = documentMapper.delete(wrapper);
        Assertions.assertEquals(18, count);
    }

    @Test
    @Order(80)
    public void testDeleteIndex() {
        boolean deleted = documentMapper.deleteIndex(EntityInfoHelper.getEntityInfo(Document.class).getIndexName());
        boolean lockDeleted = documentMapper.deleteIndex(BaseEsConstants.LOCK_INDEX);
        Assertions.assertTrue(deleted);
        Assertions.assertTrue(lockDeleted);
    }

    @Test
    @Order(79)
    public void testComplex() {
        // SQL写法
        // where business_type = 1 and (state = 9 or (state = 8 and bidding_sign = 1)) or (business_type = 2 and state in (2,3))

        // ElasticsearchClient写法
        List<FieldValue> values = Arrays.asList(WrapperProcessor.fieldValue(2), WrapperProcessor.fieldValue(3));
        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool()
                .must(a -> a.term(b -> b.field("business_type").value(1)))
                .must(a -> a.bool(b -> b
                        .must(c -> c.term(d -> d.field("state").value(9)))
                        .should(c -> c.bool(d -> d
                                .must(f -> f.term(e -> e.field("state").value(8)))
                                .must(f -> f.term(e -> e.field("bidding_sign").value(1)))
                        ))
                ))
                .should(a -> a.bool(c -> c
                        .must(d -> d.term(e -> e.field("business_type").value(2)))
                        .must(d -> d.terms(e -> e.field("state").terms(f -> f.value(values))))
                ));

        Query query = boolQueryBuilder.build()._toQuery();
        System.out.println(query.toString());
        System.out.println("--------------------");

        // MP及EE写法
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq("business_type", 1)
                .and(a -> a.eq("state", 9).or(b -> b.eq("state", 8).eq("bidding_sign", 1)))
                .or(i -> i.eq("business_type", 2).in("state", 2, 3));
        SearchRequest.Builder searchBuilder = documentMapper.getSearchBuilder(wrapper);
        Query qry = searchBuilder.build().query();
        if (qry != null) {
            System.out.println(qry);
        }
        List<Document> documents = documentMapper.selectList(wrapper);
    }

}
