package org.dromara.easyes.test.agg;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.kernel.WrapperProcessor;
import org.dromara.easyes.test.entity.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 聚合请求树测试，无需连接 Elasticsearch。
 */
public class AggregationTreeTest {
    @Test
    public void testThreeGroupsWithSum() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.groupBy("title", "subTitle", "starNum").sum("bigNum");

        Aggregation root = build(wrapper).aggregations().get("titleTerms");
        Aggregation second = child(root, "subTitleTerms");
        Aggregation third = child(second, "starNumTerms");
        assertEquals("starNum", third.terms().field());
        assertEquals("bigNum", child(third, "bigNumSum").sum().field());
    }

    @Test
    public void testTwoGroupsWithSiblingMetrics() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.termsAggregation("title", "subTitle").max("starNum").min("starNum");

        Aggregation second = child(build(wrapper).aggregations().get("titleTerms"), "subTitleTerms");
        assertEquals(2, second.aggregations().size());
        assertEquals("starNum", child(second, "starNumMax").max().field());
        assertEquals("starNum", child(second, "starNumMin").min().field());
    }

    @Test
    public void testMetricBeforeNextGroupStaysAtItsLevel() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.groupBy("title").avg("bigNum").groupBy("subTitle").sum("starNum");

        Aggregation root = build(wrapper).aggregations().get("titleTerms");
        assertEquals(2, root.aggregations().size());
        assertEquals("bigNum", child(root, "bigNumAvg").avg().field());
        Aggregation second = child(root, "subTitleTerms");
        assertEquals("starNum", child(second, "starNumSum").sum().field());
    }

    @Test
    public void testIndependentAggregationAlongsidePipeline() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.groupBy("title", "subTitle").max(false, "starNum").sum("bigNum");

        SearchRequest request = build(wrapper);
        assertEquals(2, request.aggregations().size());
        assertEquals("starNum", request.aggregations().get("starNumMax").max().field());
        Aggregation second = child(request.aggregations().get("titleTerms"), "subTitleTerms");
        assertEquals(1, second.aggregations().size());
        assertEquals("bigNum", child(second, "bigNumSum").sum().field());
    }

    @Test
    public void testSingleGroupWithMultipleMetrics() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.groupBy("title").sum("starNum").avg("bigNum");

        Aggregation root = build(wrapper).aggregations().get("titleTerms");
        assertEquals(2, root.aggregations().size());
        assertTrue(child(root, "starNumSum").isSum());
        assertTrue(child(root, "bigNumAvg").isAvg());
    }

    @Test
    public void testIndependentGroups() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.groupBy(false, "title", "subTitle");

        SearchRequest request = build(wrapper);
        assertEquals(2, request.aggregations().size());
        assertTrue(request.aggregations().get("titleTerms").aggregations().isEmpty());
        assertTrue(request.aggregations().get("subTitleTerms").aggregations().isEmpty());
    }

    private SearchRequest build(LambdaEsQueryWrapper<Document> wrapper) {
        return WrapperProcessor.buildSearchBuilder(wrapper, Document.class).build();
    }

    private Aggregation child(Aggregation parent, String name) {
        assertNotNull(parent);
        Aggregation child = parent.aggregations().get(name);
        assertNotNull(child, name);
        return child;
    }
}
