package com.xpc.easyes.sample.test.ignore;

import com.xpc.easyes.sample.EasyEsApplication;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 框架开发过程中的一些测试 请忽略
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Deprecated
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = TestEasyEsApplication.class)
public class IgnoreTest {
//    @Resource
//    DocumentMapper documentMapper;
//    @Autowired
//    private RestHighLevelClient client;
//
//
//    @Test
//    public void testSearch0() throws IOException {
//        SearchSourceBuilder builder = new SearchSourceBuilder();
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
////        boolQueryBuilder.must(QueryBuilders.termQuery("overt", Boolean.TRUE));
//        boolQueryBuilder.must(QueryBuilders.termQuery("title", "茶叶"));
//        builder.query(boolQueryBuilder);
//
//        TermsAggregationBuilder terms = AggregationBuilders.terms("overt").field("overt");
//        TermsAggregationBuilder terms1 = AggregationBuilders.terms("creator1").field("creator");
//
//        builder.aggregation(terms);
//        builder.aggregation(terms1);
//
//
//        SearchRequest request = new SearchRequest("kiplatform_library").source(builder);
//        System.out.println(request.source());
//        System.out.println("=================");
//        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//        System.out.println(search);
//    }
//
//    @Test
//    public void testSearch1() throws IOException {
//        // case1 : 单纯测 eq
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getTitle, "茶叶");
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//    }
//
//    @Test
//    public void testSearch2() throws IOException {
//        // case2: 单纯测ne
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.ne(Document::getTitle, "茶叶");
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//    }
//
//    @Test
//    public void testSearch3() throws IOException {
//        // case3: 单纯测match
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.match(Document::getContent, "茶叶");
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//    }
//
//    @Test
//    public void testSearch4() throws IOException {
//        // case4: 单纯测not match
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.notMatch(Document::getContent, "茶叶");
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//    }
//
//    @Test
//    public void testSearch5() throws IOException {
//        // case5 : 测and
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .and(w -> w.eq(Document::getTitle, "茶叶").eq(Document::getLibraryId, 253));
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//    }
//
//    @Test
//    public void testSearch6() throws IOException {
//        // case6 : 测or在内层
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .and(w -> w.eq(Document::getTitle, "狗子").or().eq(Document::getTitle, "茶叶"));
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//    }
//
//    @Test
//    public void testSearch7() throws IOException {
//        // case 7 测试 or 在外层
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.FALSE)
//                .or()
//                .eq(Document::getTitle, "狗子");
//
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//
//
//    }
//
//    @Test
//    public void testSearch8() throws IOException {
//        // case 8 测试 or 在内外皆有 虽然也不太可能会碰到
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .and(w -> w.eq(Document::getTitle, "狗子").or().eq(Document::getTitle, "茶叶"))
//                .or()
//                .eq(Document::getCreator, "邢鹏成");
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println("response:" + response);
//
//    }
//
//    @Test
//    public void testSearch9() throws IOException {
//        // case 9 测试 or 在内层有 且有多处
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .and(w -> w.eq(Document::getTitle, "狗子").or().eq(Document::getTitle, "茶叶"))
//                .and(w -> w.eq(Document::getCreator, "刘旭文").or().eq(Document::getCreator, "邢鹏成"));
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch10() throws IOException {
//        // case 9 测试 or 无处不在 虽然实际使用中几乎不太可能出现这种查询条件 但必须满足
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .and(w -> w.eq(Document::getTitle, "狗子").or().eq(Document::getTitle, "茶叶"))
//                .and(w -> w.eq(Document::getCreator, "刘旭文").or().eq(Document::getCreator, "邢鹏成"))
//                .or()
//                .eq(Document::getType, 1)
//                .or(w -> w.eq(Document::getGroupId, 1).or().eq(Document::getGroupId, 2));
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch11() throws IOException {
//        // case 11 测试范围查询
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .and(w -> w.ge(Document::getDatabaseId, 66).or().le(Document::getDatabaseId, 3407));
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch12() throws IOException {
//        // case 12 测试范围查询 between
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .between(Document::getDatabaseId, 1, 3407);
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch13() throws IOException {
//        // case 13 测试范围查询 not between
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .notBetween(Document::getDatabaseId, 3300, 3400);
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch14() throws IOException {
//        // case 14 测试高亮
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .eq(Document::getDatabaseId, 3407)
//                .highLight(Document::getTitle);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch15() throws IOException {
//        // case 15 测试排序 order by
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .orderByDesc(Document::getDatabaseId, Document::getGmtCreate);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch16() throws IOException {
//        // case 16 测试排序 order by
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .orderByDesc(Document::getDatabaseId)
//                .orderByAsc(Document::getGmtCreate);
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch17() throws IOException {
//        // case 17 测试 in
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .in(Document::getDatabaseId, 3406, 3407);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch18() throws IOException {
//        // case 18 测试 in
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .notIn(Document::getDatabaseId, 3406, 3407);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch19() throws IOException {
//        // case 19 测试 select字段
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .eq(Document::getDatabaseId, 3407);
//        wrapper.select(Document::getTitle, Document::getGmtCreate);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch20() throws IOException {
//        // case 20 测试 排除select字段
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .eq(Document::getDatabaseId, 3407);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch21() throws IOException {
//        // case 21 测试 isNull
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .isNull(Document::getContent);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch22() throws IOException {
//        // case 22 测试 notNull
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶")
//                .isNotNull(Document::getContent);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch23() throws IOException {
//        // case 22 测试 from size count
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶");
//        wrapper.from(2);
//        wrapper.size(3);
//        wrapper.select(Document::getTitle, Document::getDatabaseId);
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch24() throws IOException {
//        // case 24 测试 分页
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE)
//                .eq(Document::getTitle, "茶叶");
//        wrapper.highLight(Document::getTitle);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//        PageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 5);
//        System.out.println(pageInfo);
//    }
//
//    @Test
//    public void testSearch25() throws IOException {
//        // case 25 测试权重eq
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getContent, "茶叶", 2.0F)
//                .eq(Document::getTitle, "茶叶", 5.0F);
//        wrapper.select(Document::getDatabaseId, Document::getTitle, Document::getContent);
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        SearchResponse query = documentMapper.search(wrapper);
//        System.out.println(query);
//    }
//
//    @Test
//    public void testSearch26() throws IOException {
//        // case 26 测试权重ne
//
//    }
//
//    @Test
//    public void testSearch28() throws IOException {
//        // case 28 测试权重 like
//    }
//
//    @Test
//    public void testSearch29() throws IOException {
//        // case 26 测试权重 notLike
//
//    }
//
//    @Test
//    public void testSearch30() throws IOException {
//        // case 30 测试权重 gt
//    }
//
//    @Test
//    public void testSearch31() throws IOException {
//        // case 31 测试权重 ge
//    }
//
//    @Test
//    public void testSearch32() throws IOException {
//        // case 32 测试权重 lt
//    }
//
//    @Test
//    public void testSearch33() throws IOException {
//        // case 33 测试权重 le
//    }
//
//    @Test
//    public void testSearch34() throws IOException {
//        // case 34 测试权重 between
//    }
//
//    @Test
//    public void testSearch35() throws IOException {
//        // case 35 测试权重 notBetween
//    }
//
//    @Test
//    public void testSearch36() throws IOException {
//        // case 36 测试权重 in
//    }
//
//    @Test
//    public void testSearch37() throws IOException {
//        // case 37 测试权重 not in
//    }
//
//    @Test
//    public void testSearch38() throws IOException {
//        // case 38 测试权重 is null
//    }
//
//    @Test
//    public void testSearch39() throws IOException {
//        // case 39 测试权重 is not null
//    }
//
//    @Test
//    public void testSearch40() throws IOException {
//        // case 40 测试聚合 groupBy
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getTitle, "茶叶");
//        wrapper.groupBy(Document::getOvert, Document::getCreator);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch41() throws IOException {
//        // case 41 测试聚合 termsAggregation
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getTitle, "茶叶");
//        wrapper.termsAggregation(Document::getOvert);
//        wrapper.termsAggregation(Document::getCreator);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//
//    public void testSearch42() throws IOException {
//        // case 41 测试聚合 均值
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.avg(Document::getOvert);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch43() throws IOException {
//        // case 43 测试聚合 最大值
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.max(Document::getOvert);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch44() throws IOException {
//        // case 44 测试聚合 最小值
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.min(Document::getOvert);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch45() throws IOException {
//        // case 45 测试聚合 求和
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.sum(Document::getOvert);
//        SearchResponse response = documentMapper.search(wrapper);
//        System.out.println(response);
//    }
//
//    @Test
//    public void testSearch46() throws IOException {
//        // case 46 测试不查某字段 mp的模式
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getTitle, "茶叶");
//        wrapper.select(Document.class, info -> !Objects.equals(info.getColumn(), "groupId"));
//        SearchResponse search = documentMapper.search(wrapper);
//        System.out.println(search);
//    }
//
//    @Test
//    public void testSearch47() throws IOException {
//        // case 47 测试insert
//        Document document = new Document();
//        document.setTitle("茶叶籽");
//        document.setType(10);
//        document.setCreator("laohan");
//        int search = documentMapper.insert(document);
//        System.out.println(search);
//    }
//
//    @Test
//    public void testSearch48() throws IOException {
//        // case 48 测试pageQuery 不指定分页
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE);
//        wrapper.select(Document::getTitle);
//        PageInfo pageInfo = documentMapper.pageQuery(wrapper);
//        System.out.println(pageInfo);
//    }
//
//    @Test
//    public void testSearch49() throws IOException {
//        // case 49 测试pageQuery 指定分页参数
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE);
//        wrapper.select(Document::getTitle);
//        PageInfo pageInfo = documentMapper.pageQuery(wrapper, 2, 20);
//        System.out.println(pageInfo);
//    }
//
//    @Test
//
//    public void testSearch50() throws IOException {
//        // case 50 测试pageQuery 指定返回类型 不指定分页参数
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE);
//        wrapper.select(Document::getTitle);
//        PageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper);
//        System.out.println(pageInfo);
//    }
//
//    @Test
//    public void testSearch51() throws IOException {
//        // case 51 测试pageQuery 指定分页及返回类型
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getOvert, Boolean.TRUE);
//        wrapper.select(Document::getTitle);
//        PageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 3);
//        System.out.println(pageInfo);
//    }
//
//    @Test
//    public void testSearch52() throws IOException {
//        // case 52 测试 deleteById
//        int num = documentMapper.deleteById("frmpgnwBUP1SGuce55Qg");
//        System.out.println(num);
//    }
//
//    @Test
//    public void testSearch53() throws IOException {
//        // case 53 测试 delete
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getCreator, "laohan");
//        int delete = documentMapper.delete(wrapper);
//        System.out.println(delete);
//    }
//
//    @Test
//    public void testSearch54() throws IOException {
//        // case 54 测试 deleteBatchIds
//        Set<String> ids = new HashSet<>();
//        ids.add("h7lokXwBUP1SGuceQJQp");
//        ids.add("hLlekXwBUP1SGuceQpRW");
//        ids.add("hblekXwBUP1SGucehJTO");
//        int num = documentMapper.deleteBatchIds(ids);
//        System.out.println(num);
//    }
//
//    @Test
//    public void testSearch55() throws IOException {
//        // case 55 测试 selectById
//        Document document = documentMapper.selectById("hrlnkXwBUP1SGucenpQn");
//        System.out.println(document);
//    }
//
//    @Test
//    public void testSearch56() throws IOException {
//        // case 56 测试 selectBatchIds
//        Set<String> ids = new HashSet<>();
//        ids.add("hrlnkXwBUP1SGucenpQn");
////        ids.add("iLlokXwBUP1SGuce2JQi");
//        List<Document> documents = documentMapper.selectBatchIds(ids);
//        System.out.println(documents);
//    }
//
//    @Test
//    public void testSearch57() throws IOException {
//        // case 57 测试 selectOne
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.eq(Document::getType, 10);
//        Document document = documentMapper.selectOne(wrapper);
//        System.out.println(document);
//    }
//
//    @Test
//    public void testSearch58() throws IOException {
//        // case 58 测试 updateById
//        Document document = new Document();
//        document.setId("9098d78f-5d18-47f4-be68-de539cf4c1fd");
//        document.setTitle("茶叶孜孜2");
//        document.setCreator("laohan");
//        document.setNotExistsField("666");
//        int i = documentMapper.updateById(document);
//        System.out.println(i);
//    }
//
//    @Test
//    public void testSearch59() throws IOException {
//        // case 59 测试 update
//        LambdaEsUpdateWrapper<Document> updateWrapper = new LambdaEsUpdateWrapper<>();
//        updateWrapper.eq(Document::getType, 10)
//                .set(Document::getTitle, "茶叶吱吱吱");
//        int update = documentMapper.update(null, updateWrapper);
//        System.out.println(update);
//    }
//
//    @Test
//    public void testSearch60() throws IOException {
//        // case 60 测试 update
//        LambdaEsUpdateWrapper<Document> updateWrapper = new LambdaEsUpdateWrapper<>();
//        updateWrapper.eq(Document::getType, 10);
//        Document document = new Document();
//        document.setCreator("");
//        document.setSourceTitle("");
//        int update = documentMapper.update(document, updateWrapper);
//        System.out.println(update);
//    }
//
//    @Test
//    public void testSearch61() throws IOException {
//        // case 61 测试 createIndex
//        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
//        wrapper.indexName("my_test");
//        wrapper.settings(3, 1);
//        wrapper.createAlias("my_test", "my_alias");
//        wrapper.mapping(Document::getTitle, FieldType.KEYWORD)
//                .mapping(Document::getContent, FieldType.TEXT);
//        boolean isOk = documentMapper.createIndex(wrapper);
//        System.out.println(isOk);
//    }
//
//    @Test
//    public void testSearch62() throws IOException {
//        // case 62 测试 delIndex
//        boolean res = documentMapper.deleteIndex("my_test");
//        System.out.println(res);
//    }
//
//    @Test
//    public void testSearch63() throws IOException {
//        // case 63 测试 updateIndex
//        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
//        wrapper.indexName("my_test");
//        wrapper.mapping(Document::getId, FieldType.LONG);
//        wrapper.mapping(Document::getOvert, FieldType.INTEGER);
//        wrapper.mapping(Document::getCreator, FieldType.KEYWORD);
//        boolean res = documentMapper.updateIndex(wrapper);
//        System.out.println(res);
//    }
//
//    @Test
//    public void testSearch64() throws IOException {
//        // case 64 测试 existsIndex
//        boolean res = documentMapper.existsIndex("my_test");
//        System.out.println(res);
//    }
//
//    @Test
//    public void testSearch65() throws IOException {
//        // case 65 测试 like left
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.likeLeft(Document::getCreator, "鹏成");
//        List<Document> documents = documentMapper.selectList(wrapper);
//        System.out.println(documents);
//    }
//
//    @Test
//    public void testSearch66() throws IOException {
//        // case 66 测试 like right
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.likeRight(Document::getCreator, "邢");
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        List<Document> documents = documentMapper.selectList(wrapper);
//        System.out.println(documents);
//    }
//
//    @Test
//    public void testSearch67() throws IOException {
//        // case 67 测试 like
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.like(Document::getCreator, "鹏");
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        List<Document> documents = documentMapper.selectList(wrapper);
//        System.out.println(documents);
//    }
//
//    @Test
//    public void testSearch68() throws IOException {
//        // case 68 测试 not like
//        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
//        wrapper.notLike(Document::getCreator, "鹏");
//        String source = documentMapper.getSource(wrapper);
//        System.out.println(source);
//        List<Document> documents = documentMapper.selectList(wrapper);
//        System.out.println(documents);
//    }
//
//    @Test
//    public void testSearch69() throws IOException {
//        Document document = documentMapper.selectById("hrlnkXwBUP1SGucenpQn");
//        System.out.println(document);
//    }
//
//
//    @Test
//    public void testSearch70() throws IOException {
//        DocumentMapper documentMapper = (DocumentMapper) Proxy.newProxyInstance(EsMapperProxy.class.getClassLoader(), new Class<?>[]{DocumentMapper.class}, new EsMapperProxy<>(Document.class));
//        Document document = documentMapper.selectById("hrlnkXwBUP1SGucenpQn");
//        System.out.println(document);
//    }
//
//    @Test
//    public void testSearch71() throws IOException {
//        Document document = documentMapper.selectById("hrlnkXwBUP1SGucenpQn");
//        System.out.println(document);
//    }
}
