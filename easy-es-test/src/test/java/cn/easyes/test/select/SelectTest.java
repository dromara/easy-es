package cn.easyes.test.select;

import cn.easyes.common.enums.Link;
import cn.easyes.common.enums.Query;
import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.toolkit.EsWrappers;
import cn.easyes.core.toolkit.QueryUtils;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class SelectTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testSelect() {
        // 测试根据条件查询
        String title = "老汉";
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, title);
        // 字段名亦可指定为字符串,不推荐
//        wrapper.eq("title",title);
        wrapper.limit(1);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
        Assertions.assertEquals(title, document.getTitle());
    }

    @Test
    public void testAllEq() {
        // 多字段批量查询 对标mp的allEq 直接在LambdaEsQueryWrapper中指定即可
        Map<String, Object> map = new HashMap<>();
        map.put("title", "老汉");
        map.put("creator", "吃饭");
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.allEq(map);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testLambdaSelect() {
        // 类似MP的lambda链式查询
        List<Document> documents = documentMapper.selectList(EsWrappers.lambdaQuery(Document.class).eq(Document::getTitle, "老汉"));
        System.out.println(documents);
    }

    @Test
    public void testSelectById() {
        // 测试根据id查询
        String id = "5";
        Document document = documentMapper.selectById(id);
        System.out.println(document);
    }

    @Test
    public void testSelectList() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老汉";
        wrapper.eq(Document::getTitle, title);
        wrapper.select(Document::getCreator);
        wrapper.notSelect(Document::getContent, Document::getGmtCreate);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testSelectBatchIds() {
        List<String> ids = Arrays.asList("OmEQCIAB0E2Rzy0qHFNV", "UykMUX0BUP1SGucePGhx");
        List<Document> documents = documentMapper.selectBatchIds(ids);
        System.out.println(documents);
    }

    @Test
    public void testTrackTotalHits() {
        // 查询超过1w条时,trackTotalHits=true 会自动开启
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.limit(20000);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
    }

    @Test
    public void testMatch() {
        // 会对输入做分词,只要所有分词中有一个词在内容中有匹配就会查询出该数据,无视分词顺序
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "过硬");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
    }

    @Test
    public void testMatchPhase() {
        // 会对输入做分词，但是需要结果中也包含所有的分词，而且顺序要求一样,否则就无法查询出结果
        // 例如es中数据是 技术过硬,如果搜索关键词为过硬技术就无法查询出结果
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrase(Document::getContent, "技术");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMatchAllQuery() {
        // 查询所有数据,类似mysql select all.
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchAllQuery();
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMatchPhrasePrefixQuery() {
        // 前缀匹配查询 查询字符串的最后一个词才能当作前缀使用
        // 前缀 可能会匹配成千上万的词,这不仅会消耗很多系统资源,而且结果的用处也不大,所以可以提供入参maxExpansions,若不写则默认为50
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrasePrefixQuery(Document::getCustomField, "乌拉巴拉", 10);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMultiMatchQuery() {
        // 从多个指定字段中查询包含老王的数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.multiMatchQuery("老王", Document::getTitle, Document::getContent, Document::getCreator, Document::getCustomField);

        // 其中,默认的Operator为OR,默认的minShouldMatch为60% 这两个参数都可以按需调整,我们api是支持的 例如:
        // 其中AND意味着所有搜索的Token都必须被匹配,OR表示只要有一个Token匹配即可. minShouldMatch 80 表示只查询匹配度大于80%的数据
        // wrapper.multiMatchQuery("老王",Operator.AND,80,Document::getCustomField,Document::getContent);

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
        System.out.println(documents);
    }

    @Test
    public void testQueryStringQuery() {
        // 从所有字段中查询包含关键词老汉的数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.queryStringQuery("老汉");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testQueryStringQueryMulti() {
        // 假设我的查询条件是:创建者等于老王,且创建者分词匹配"隔壁"(比如:隔壁老汉,隔壁老王),或者创建者包含猪蹄
        // 对应mysql语法是(creator="老王" and creator like "老王") or creator like "%猪蹄%",下面用es的queryString来演示实现一样的效果
        // 足够灵活,非常适合前端页面中的查询条件列表字段及条件不固定,且可选"与或"的场景.
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String queryStr = QueryUtils.combine(Link.OR,
                QueryUtils.buildQueryString(Document::getCreator, "老王", Query.EQ, Link.AND),
                QueryUtils.buildQueryString(Document::getCreator, "隔壁", Query.MATCH))
                + QueryUtils.buildQueryString(Document::getCreator, "*猪蹄*", Query.EQ);
        wrapper.queryStringQuery(queryStr);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testPrefixQuery() {
        // 查询创建者以"隔壁"打头的所有数据  比如隔壁老王 隔壁老汉 都能被查出来
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.prefixQuery(Document::getCreator, "隔壁");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testIn() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.in(Document::getEsId, "2", "3");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

}
