package org.dromara.easyes.test.mix;


import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 混合查询几种使用案例
 *
 * @ProductName: Hundsun HEP
 * @ProjectName: easy-es
 * @Package: cn.easyes.test.mix
 * @Description: note
 * @Author: xingpc37977
 * @Date: 2023/3/13 10:48
 * @UpdateUser: xingpc37977
 * @UpdateDate: 2023/3/13 10:48
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2023 Hundsun Technologies Inc. All Rights Reserved
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class MixTest {
    @Resource
    private DocumentMapper documentMapper;

    /**
     * 正确使用姿势0(最实用)：EE满足的语法,直接用,不满足的可以构造原生QueryBuilder
     *
     * @since 2.0.0-beta2
     */
    @Test
    public void testMix0() {
        // 查询标题为老汉，内容匹配 推*，且最小匹配度不低于80%的数据
        // 当前我们提供的开箱即用match并不支持设置最小匹配度,此时就可以自己去构造一个matchQueryBuilder来实现
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("content", "推*").minimumShouldMatch("80%");
        wrapper.eq(Document::getTitle, "老汉").mix(queryBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    /**
     * 混合查询正确使用姿势1: EE提供的功能不支持某些过细粒度的功能,所有查询条件通过原生语法构造,仅利用EE提供的数据解析功能
     */
    @Test
    public void testMix1() {
        // RestHighLevelClient原生语法
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("content", "推*").minimumShouldMatch("80%"));

        // 仅利用EE查询并解析数据功能
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.setSearchSourceBuilder(searchSourceBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }


    /**
     * 混合查询正确使用姿势2: 其它都能支持,仅排序器不支持,这种情况可以只按ES原生语法构造所需排序器SortBuilder,其它用EE完成
     */
    @Test
    public void testMix2() {
        // EE满足的语法
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .match(Document::getContent, "推*");

        // RestHighLevelClient原生语法
        Script script = new Script("doc['star_num'].value");
        ScriptSortBuilder scriptSortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER).order(SortOrder.DESC);

        // 利用EE查询并解析数据
        wrapper.sort(scriptSortBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    /**
     * 混合查询正确使用姿势3: 其它功能都能支持,但需要向SearchSourceBuilder中追加非query参数
     */
    @Test
    public void testMix3() {
        // EE满足的语法
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .match(Document::getContent, "推*");
        SearchSourceBuilder searchSourceBuilder = documentMapper.getSearchSourceBuilder(wrapper);

        // 追加或者设置一些SearchSourceBuilder支持但EE暂不支持的参数 不建议追加query参数,因为如果追加query参数会直接覆盖上面EE已经帮你生成好的query,以最后set的query为准
        searchSourceBuilder.timeout(TimeValue.timeValueSeconds(3L));
        wrapper.setSearchSourceBuilder(searchSourceBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }


    /**
     * 查询条件中可以利用大多数基本查询,但EE提供的聚合功能不能满足需求的情况下,需要自定义聚合器
     */
    @Test
    public void textMix4() {
        // EE满足的语法
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .match(Document::getContent, "推*");
        SearchSourceBuilder searchSourceBuilder = documentMapper.getSearchSourceBuilder(wrapper);

        // RestHighLevelClient原生语法
        AggregationBuilder aggregation = AggregationBuilders.terms("titleAgg")
                .field("title");
        searchSourceBuilder.aggregation(aggregation);
        wrapper.setSearchSourceBuilder(searchSourceBuilder);
        SearchResponse searchResponse = documentMapper.search(wrapper);
        // tip: 聚合后的信息是动态的,框架无法解析,需要用户根据聚合器类型自行从桶中解析,参考RestHighLevelClient官方Aggregation解析文档
    }


    /**
     * 不支持的混合查询1: 追加覆盖问题
     */
    @Test
    public void textNotSupportMix() {
        // EE满足的语法
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .match(Document::getContent, "推*");
        SearchSourceBuilder searchSourceBuilder = documentMapper.getSearchSourceBuilder(wrapper);

        // 用户又想在上面的基础上,再追加一些个性化的查询参数进去 但实际上此时执行查询时,查询条件仅仅是最后设置的title=隔壁老王,前面的老汉推*会被覆盖
        searchSourceBuilder.query(QueryBuilders.matchQuery("title", "隔壁老王"));
        wrapper.setSearchSourceBuilder(searchSourceBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
        // 思考: 为什么会被覆盖? 因为目前技术上做不到,查询树已经建立好了,es底层并没有提供向树的指定层级上继续追加查询条件的API
    }

    /**
     * 不支持的混合查询2: 脱裤子放P 自欺欺人系列
     */
    @Test
    public void testNotSupportMix2() {
        // EE满足的语法
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .match(Document::getContent, "推*");

        // SearchSourceBuilder的构造是自己new出来的,不是通过mapper.getSearchSourceBuilder(wrapper)构造 相当于脱裤子放P,那么上面的查询条件老汉推*自然不会生效
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.minScore(10.5f);
        wrapper.setSearchSourceBuilder(searchSourceBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }


}
