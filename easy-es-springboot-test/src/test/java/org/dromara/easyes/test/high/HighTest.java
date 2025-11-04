package org.dromara.easyes.test.high;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.dromara.easyes.common.utils.jackson.JsonUtils;
import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.biz.OrderByParam;
import org.dromara.easyes.core.biz.SAPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.kernel.EsWrappers;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 高阶语法测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class HighTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testFilterField() {
        // 测试只查询指定字段
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老汉";
        wrapper.eq(Document::getTitle, title);
        wrapper.select(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
    }

    @Test
    public void testNotFilterField() {
        // 测试不查询指定字段 (推荐)
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老汉";
        wrapper.eq(Document::getTitle, title);
        wrapper.notSelect(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);

        // 另外一种与mp一致语法的Lambda写法
        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.select(Document.class, d -> !Objects.equals(d.getColumn(), "title"));
        Document document1 = documentMapper.selectOne(wrapper);
        System.out.println(document1);
    }

    @Test
    public void testOrderBy() {
        // 测试排序 为了测试排序,我们在Document对象中新增了创建时间字段,更新了索引,并新增了两条数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getContent, "推");
        wrapper.select(Document::getTitle, Document::getGmtCreate);
        List<Document> before = documentMapper.selectList(wrapper);
        System.out.println("before:" + before);
        wrapper.orderByDesc(Document::getGmtCreate);
        List<Document> desc = documentMapper.selectList(wrapper);
        System.out.println("desc:" + desc);
    }

    @Test
    public void testGroupBy() throws IOException {
        // 测试聚合
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getContent, "推");
        wrapper.groupBy(Document::getTitle);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testMatch() {
        // 测试分词查询
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        wrapper.match(Document::getContent, keyword);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testWeight() throws IOException {
        // 测试权重
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        float contentBoost = 5.0f;
        wrapper.match(Document::getContent, keyword, contentBoost);
        String creator = "老汉";
        float creatorBoost = 2.0f;
        wrapper.eq(Document::getCreator, creator, creatorBoost);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }

    @Test
    public void testHighlight() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "技术";
        wrapper.match(Document::getContent, keyword);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testPageQuery() {
        // 浅分页,适合数据量少于1w的情况
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getTitle, "老汉");
        EsPageInfo<Document> documentPageInfo = documentMapper.pageQuery(wrapper, 2, 10);
        System.out.println(documentPageInfo);
    }


    @Test
    public void testSearchAfter() {
        // SearchAfter分页,适合大数据量以及有跳页的场景
        LambdaEsQueryWrapper<Document> lambdaEsQueryWrapper = EsWrappers.lambdaQuery(Document.class);
        lambdaEsQueryWrapper.size(10);
        lambdaEsQueryWrapper.orderByDesc(Document::getEsId, Document::getStarNum);
        SAPageInfo<Document> saPageInfo = documentMapper.searchAfterPage(lambdaEsQueryWrapper, null, 10);
        //第一页
        System.out.println(saPageInfo);
        //获取下一页
        List<FieldValue> nextSearchAfter = saPageInfo.getNextSearchAfter();
        SAPageInfo<Document> documentSAPageInfo = documentMapper.searchAfterPage(lambdaEsQueryWrapper, nextSearchAfter, 10);
        System.out.println(documentSAPageInfo);
    }

    @Test
    public void testSortByScore() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "技术");
        wrapper.sortByScore(SortOrder.Asc);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testOrderByParams() {
        // 此处假设此参数由前端通过xxQuery类传入,排序根据标题降序,根据内容升序
        String jsonParam = "[{\"order\":\"title.keyword\",\"sort\":\"DESC\"},{\"order\":\"creator.keyword\",\"sort\":\"ASC\"}]";
        List<OrderByParam> orderByParams = JsonUtils.toList(jsonParam, OrderByParam.class);
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "技术")
                .orderBy(orderByParams);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testSort() {
        // 测试复杂排序,SortBuilder的子类非常多,这里仅演示一种, 比如有用户提出需要随机获取数据 0.9.7+
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "技术");
        wrapper.sort(SortOptions.of(a -> a.script(b -> b
                .type(ScriptSortType.Number)
                .script(c -> c.inline(e -> e.source("Math.random()")))
        )));
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

}
