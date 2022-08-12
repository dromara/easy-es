package cn.easyes.test.all;

import cn.easyes.common.constants.BaseEsConstants;
import cn.easyes.core.biz.OrderByParam;
import cn.easyes.core.biz.PageInfo;
import cn.easyes.core.biz.SAPageInfo;
import cn.easyes.core.cache.GlobalConfigCache;
import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.conditions.LambdaEsUpdateWrapper;
import cn.easyes.core.toolkit.EntityInfoHelper;
import cn.easyes.core.toolkit.EsWrappers;
import cn.easyes.core.toolkit.FieldUtils;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Circle;
import org.elasticsearch.geometry.Point;
import org.elasticsearch.geometry.Rectangle;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedMin;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 全部核心功能测试-除手动挡索引相关API
 * 以下测试用例,需要开启自动挡
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@DisplayName("easy-es核心功能测试用例")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class AllTest {
    @Resource
    private DocumentMapper documentMapper;

    // 1.新增
    @Test
    @Order(1)
    public void testInsert() {
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
        int successCount = documentMapper.insert(document);
        Assertions.assertEquals(successCount, 1);

    }

    @Test
    @Order(2)
    public void testBatchInsert() {
        List<Document> documentList = new ArrayList<>();
        for (int i = 2; i < 23; i++) {
            Integer sec = i;
            Document document = new Document();
            document.setEsId(sec.toString());
            document.setTitle("测试文档" + i);
            document.setContent("测试内容" + i);
            document.setCreator("老汉" + i);
            document.setGmtCreate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            document.setCustomField("自定义字段" + i);
            Point point = new Point(13.400544 + i, 52.530286 + i);
            document.setGeoLocation(point.toString());
            document.setStarNum(i);
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
        int count = documentMapper.update(null, wrapper);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(5)
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
    @Order(6)
    public void testSelectOne() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "内容")
                .orderByAsc(Document::getStarNum, Document::getEsId)
                .limit(1);
        Document document = documentMapper.selectOne(wrapper);
        Assertions.assertEquals("测试文档1标题被更新了", document.getTitle());
    }

    @Test
    @Order(6)
    public void testSelectById() {
        Document document = documentMapper.selectById(1);
        Assertions.assertEquals("1", document.getEsId());
        Assertions.assertEquals("老汉1", document.getCreator());
    }

    @Test
    @Order(6)
    public void testSelectBatchIds() {
        List<Document> documents = documentMapper.selectBatchIds(Arrays.asList("1", "2"));
        Assertions.assertEquals(2, documents.size());
        Assertions.assertEquals("1", documents.get(1).getEsId());
        Assertions.assertEquals("老汉2", documents.get(0).getCreator());
    }

    @Test
    @Order(6)
    public void testSelectList() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCustomField, "字段");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testSelectCount() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCustomField, "字段");
        Long count = documentMapper.selectCount(wrapper);
        Assertions.assertEquals(22L, count);
    }

    @Test
    @Order(6)
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
    @Order(6)
    public void testConditionEq() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "测试文档10");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
        Assertions.assertEquals("测试文档10", documents.get(0).getTitle());
    }

    @Test
    @Order(6)
    public void testConditionNe() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.ne(Document::getTitle, "测试文档10");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(21, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionGt() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.gt(Document::getStarNum, 20);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionGe() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.ge(Document::getStarNum, 20);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(3, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionLt() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.lt(Document::getStarNum, 3);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionLe() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.le(Document::getStarNum, 3);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(3, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionBetween() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.between(Document::getStarNum, 1, 10);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(10, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionNotBetween() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.notBetween(Document::getStarNum, 1, 10);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(12, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionLike() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.like(Document::getTitle, "试文档");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionNotLike() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.notLike(Document::getTitle, "试文档");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(0, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionLikeLeft() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeLeft(Document::getTitle, "文档10");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionLikeRight() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getTitle, "测试文");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionIsNull() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.isNull(Document::getNullField);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(21, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionIsNotNull() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.isNotNull(Document::getNullField);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(6)
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
    @Order(6)
    public void testConditionNotIn() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.notIn(Document::getStarNum, 1, 2);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(20, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionGroupBy() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .groupBy(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        ParsedLongTerms parsedLongTerms = response.getAggregations()
                .get("starNum");
        for (Terms.Bucket bucket : parsedLongTerms.getBuckets()) {
            Assertions.assertTrue(bucket.getKey().equals(1L) && bucket.getDocCount() == 2L);
            break;
        }
    }

    @Test
    @Order(6)
    public void testConditionMax() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .max(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        ParsedMax parsedMax = response.getAggregations()
                .get("starNum");
        Assertions.assertTrue(parsedMax.getValue() > 21);
    }

    @Test
    @Order(6)
    public void testConditionMin() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .min(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        ParsedMin parsedMin = response.getAggregations()
                .get("starNum");
        Assertions.assertTrue(parsedMin.getValue() > 0 && parsedMin.getValue() < 2);
    }

    @Test
    @Order(6)
    public void testConditionSum() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .sum(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        ParsedSum parsedSum = response.getAggregations()
                .get("starNum");
        Assertions.assertTrue(parsedSum.getValue() >= 252);
    }


    @Test
    @Order(6)
    public void testConditionAvg() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .avg(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        ParsedAvg parsedAvg = response.getAggregations()
                .get("starNum");
        Assertions.assertTrue(parsedAvg.getValue() > 11 && parsedAvg.getValue() < 12);
    }


    @Test
    @Order(6)
    public void testConditionDistinct() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试")
                .distinct(Document::getStarNum);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(21, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionLimit() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉")
                .limit(2, 5);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(5, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionFromAndSize() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉")
                .from(20)
                .size(2);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionIndex() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉")
                .index(EntityInfoHelper.getEntityInfo(Document.class).getIndexName());
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionEnableMust2Filter() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉")
                .enableMust2Filter(true);
        String source = documentMapper.getSource(wrapper);
        System.out.println(source);
        Assertions.assertTrue(source.contains("filter"));
    }

    @Test
    @Order(6)
    public void testConditionAnd() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.in(Document::getStarNum, 1, 2, 3, 4, 10, 11)
                .and(w -> w.eq(Document::getTitle, "测试文档10").match(Document::getCreator, "老汉"));
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionOrInner() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.in(Document::getStarNum, 1, 2, 3, 4, 10, 11)
                .and(w -> w.eq(Document::getTitle, "测试文档10").or().eq(Document::getTitle, "测试文档3"));
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(2, documents.size());
    }

    @Test
    @Order(6)
    public void testConditionOrOuter() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "测试文档10")
                .or()
                .in(Document::getEsId, 1, 2, 3);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(4, documents.size());
    }


    @Test
    @Order(6)
    public void testPageQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        PageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 5);
        Assertions.assertEquals(5, pageInfo.getSize());
        Assertions.assertEquals(22, pageInfo.getTotal());
    }


    @Test
    @Order(6)
    public void testSearchAfter() {
        LambdaEsQueryWrapper<Document> lambdaEsQueryWrapper = EsWrappers.lambdaQuery(Document.class);
        lambdaEsQueryWrapper.size(10);
        lambdaEsQueryWrapper.orderByDesc(Document::getEsId, Document::getStarNum);
        SAPageInfo<Document> saPageInfo = documentMapper.searchAfterPage(lambdaEsQueryWrapper, null, 10);
        //第一页
        System.out.println(saPageInfo);
        Assertions.assertEquals(10, saPageInfo.getList().size());

        //获取下一页
        List<Object> nextSearchAfter = saPageInfo.getNextSearchAfter();
        SAPageInfo<Document> next = documentMapper.searchAfterPage(lambdaEsQueryWrapper, nextSearchAfter, 10);
        Assertions.assertEquals(10, next.getList().size());
    }

    @Test
    @Order(6)
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
    @Order(6)
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
    @Order(6)
    public void testOrderByDesc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        wrapper.orderByDesc(Document::getStarNum);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("22", documents.get(0).getEsId());
        Assertions.assertEquals("1", documents.get(21).getEsId());
    }

    @Test
    @Order(6)
    public void testOrderByAsc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        wrapper.orderByAsc(Document::getStarNum, Document::getEsId);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("1", documents.get(0).getEsId());
        Assertions.assertEquals("22", documents.get(21).getEsId());
    }

    @Test
    @Order(6)
    public void testOrderBy() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        List<OrderByParam> orderByParams = new ArrayList<>();
        OrderByParam orderByParam = new OrderByParam();
        orderByParam.setOrder("starNum");
        orderByParam.setSort("DESC");
        orderByParams.add(orderByParam);
        wrapper.orderBy(orderByParams);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("22", documents.get(0).getEsId());
        Assertions.assertEquals("1", documents.get(21).getEsId());
    }

    @Test
    @Order(6)
    public void testOrderByDistanceAsc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint centerPoint = new GeoPoint(41.0, 116.0);
        wrapper.match(Document::getCreator, "老汉")
                .geoDistance(Document::getLocation, 168.8, centerPoint)
                .orderByDistanceAsc(Document::getLocation, centerPoint);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("3",documents.get(0).getEsId());
        Assertions.assertEquals("4",documents.get(3).getEsId());
    }

    @Test
    @Order(6)
    public void testOrderByDistanceDesc() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint centerPoint = new GeoPoint(41.0, 116.0);
        wrapper.match(Document::getCreator, "老汉")
                .geoDistance(Document::getLocation, 168.8, centerPoint)
                .orderByDistanceDesc(Document::getLocation, centerPoint);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("4",documents.get(0).getEsId());
        Assertions.assertEquals("3",documents.get(3).getEsId());
    }

    @Test
    @Order(6)
    public void testSortByScore() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉11");
        wrapper.sortByScore();
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("11", documents.get(0).getEsId());
    }

    @Test
    @Order(6)
    public void testSort() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        FieldSortBuilder fieldSortBuilder = SortBuilders.
                fieldSort(FieldUtils.getRealField(
                        FieldUtils.val(Document::getStarNum),
                        EntityInfoHelper.getEntityInfo(Document.class).getMappingColumnMap(),
                        GlobalConfigCache.getGlobalConfig().getDbConfig()));
        fieldSortBuilder.order(SortOrder.DESC);
        wrapper.sort(fieldSortBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("22", documents.get(0).getEsId());
        Assertions.assertEquals("1", documents.get(21).getEsId());
    }

    @Test
    @Order(6)
    public void testMatch() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testNotMatch() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.notMatch(Document::getCreator, "老汉");
        wrapper.orderByAsc(Document::getStarNum);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(0, documents.size());
    }

    @Test
    @Order(6)
    public void testMatchPhrase() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrase(Document::getContent, "测试");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertTrue(documents.size() > 0);

        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.matchPhrase(Document::getContent, "内容测试");
        List<Document> documents1 = documentMapper.selectList(wrapper1);
        Assertions.assertTrue(documents1.size() <= 0);
    }

    @Test
    @Order(6)
    public void testMatchAllQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchAllQuery();
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testMatchPhrasePrefixQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrasePrefixQuery(Document::getContent, "测试");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
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
    @Order(6)
    public void testQueryStringQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.queryStringQuery("老汉");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testPrefixQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.prefixQuery(Document::getContent, "测试");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    @Test
    @Order(6)
    public void testWeight() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "更新", 2.0f)
                .or()
                .match(Document::getCreator, "老汉");
        wrapper.sortByScore();
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals("2", documents.get(0).getEsId());
    }

    @Test
    @Order(6)
    public void testHighLight() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "测试");
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertTrue(documents.get(0).getHighlightContent().contains("测试"));
    }

    @Test
    @Order(6)
    public void testGeoBoundingBox() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint leftTop = new GeoPoint(41.187328D, 115.498353D);
        GeoPoint bottomRight = new GeoPoint(39.084509D, 117.610461D);
        wrapper.geoBoundingBox(Document::getLocation, leftTop, bottomRight);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(4, documents.size());
    }

    @Test
    @Order(6)
    public void testNotInGeoBoundingBox() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint leftTop = new GeoPoint(41.187328D, 115.498353D);
        GeoPoint bottomRight = new GeoPoint(39.084509D, 117.610461D);
        wrapper.notInGeoBoundingBox(Document::getLocation, leftTop, bottomRight);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(18, documents.size());
    }

    @Test
    @Order(6)
    public void testGeoDistance() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint geoPoint = new GeoPoint(41.0, 116.0);
        wrapper.geoDistance(Document::getLocation, 168.8, DistanceUnit.KILOMETERS, geoPoint);
        GeoDistanceSortBuilder geoDistanceSortBuilder = SortBuilders.geoDistanceSort(FieldUtils.val(Document::getLocation), geoPoint)
                .unit(DistanceUnit.KILOMETERS)
                .geoDistance(GeoDistance.ARC)
                .order(SortOrder.DESC);

        wrapper.sort(geoDistanceSortBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(4, documents.size());
    }

    @Test
    @Order(6)
    public void testGeoPolygon() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        List<GeoPoint> geoPoints = new ArrayList<>();
        GeoPoint geoPoint = new GeoPoint(40.178012, 116.577188);
        GeoPoint geoPoint1 = new GeoPoint(40.169329, 116.586315);
        GeoPoint geoPoint2 = new GeoPoint(40.178288, 116.591813);
        geoPoints.add(geoPoint);
        geoPoints.add(geoPoint1);
        geoPoints.add(geoPoint2);
        wrapper.geoPolygon(Document::getLocation, geoPoints);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(1, documents.size());
    }

    @Test
    @Order(6)
    public void testGeoShape() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        Circle circle = new Circle(13, 14, 100);
        wrapper.geoShape(Document::getGeoLocation, circle, ShapeRelation.DISJOINT);
        List<Document> documents = documentMapper.selectList(wrapper);
        Assertions.assertEquals(22, documents.size());
    }

    // 4.删除
    @Test
    @Order(7)
    public void testDeleteById() {
        int count = documentMapper.deleteById("1");
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(8)
    public void testDeleteBatchIds() {
        List<String> idList = Arrays.asList("2", "3", "4");
        int count = documentMapper.deleteBatchIds(idList);
        Assertions.assertEquals(3, count);
    }

    @Test
    @Order(9)
    public void testDeleteByWrapper() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getCreator, "老汉");
        int count = documentMapper.delete(wrapper);
        Assertions.assertEquals(18, count);
    }

    @Test
    @Order(10)
    public void testDeleteIndex() {
        boolean deleted = documentMapper.deleteIndex(EntityInfoHelper.getEntityInfo(Document.class).getIndexName());
        boolean lockDeleted = documentMapper.deleteIndex(BaseEsConstants.LOCK_INDEX);
        Assertions.assertTrue(deleted);
        Assertions.assertTrue(lockDeleted);
    }

}
