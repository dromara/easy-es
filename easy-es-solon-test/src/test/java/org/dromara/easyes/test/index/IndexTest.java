package org.dromara.easyes.test.index;

import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.core.conditions.index.LambdaEsIndexWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 不了解Es索引概念的建议先去了解 懒汉可以简单理解为MySQL中的一张表
 * 索引测试 注意,此测试类下所有方法请先关闭自动挡模式,开启手动挡 配置: process-index-mode=manual
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Disabled
@SolonTest(classes = TestEasyEsApplication.class)
public class IndexTest {
    @Inject
    private DocumentMapper documentMapper;

    /**
     * 测试创建索引 根据实体类字段及其注解配置创建索引 大多数场景适用,最为简单,但灵活性稍差
     * 创建的索引与自动挡-运动模式一样,但触发方式为手动调用 区别是自动挡模式下索引创建及更新随spring容器启动时自动执行
     */
    @Test
    public void testCreateIndexByEntity() {
        // 绝大多数场景推荐使用 简单至上
        boolean ok = documentMapper.createIndex();
        Assertions.assertTrue(ok);
    }

    /**
     * 测试创建索引 根据自定义信息去创建,最为灵活,用此种方式可支持任何es支持的索引
     */
    @Test
    public void testCreateIndex() {
        // 复杂场景使用
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 此处简单起见 索引名称须保持和实体类名称一致,字母小写 后面章节会教大家更如何灵活配置和使用索引
        wrapper.indexName(Document.class.getSimpleName().toLowerCase());

        // 此处将文章标题映射为keyword类型(不支持分词),文档内容映射为text类型(支持分词查询)
        wrapper.mapping(Document::getTitle, FieldType.KEYWORD, 2.0f)
                .mapping(Document::getLocation, FieldType.GEO_POINT)
                .mapping(Document::getGeoLocation, FieldType.GEO_SHAPE)
                .mapping(Document::getContent, FieldType.TEXT, Analyzer.IK_SMART, Analyzer.IK_MAX_WORD);

        // 0.9.8+版本,增加对符串字段名称的支持,Document实体中须在对应字段上加上@Tablefield(value="wu-la")用于映射此字段值
        wrapper.mapping("wu-la", FieldType.TEXT, Analyzer.IK_MAX_WORD, Analyzer.IK_MAX_WORD);

        // 设置分片及副本信息,可缺省
        wrapper.settings(3, 2);

        // 设置别名信息,可缺省
        String aliasName = "daily";
        wrapper.createAlias(aliasName);

        // 设置父子信息,若无父子文档关系则无需设置
        wrapper.join("joinField", "document", "comment");

        // 创建索引
        boolean isOk = documentMapper.createIndex(wrapper);
        Assertions.assertTrue(isOk);
    }

    @Test
    public void testExistsIndex() {
        // 测试是否存在指定名称的索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean existsIndex = documentMapper.existsIndex(indexName);
        Assertions.assertTrue(existsIndex);
    }

    @Test
    public void testGetIndex() {
        GetIndexResponse indexResponse = documentMapper.getIndex();
        // 这里打印下索引结构信息 其它分片等信息皆可从indexResponse中取
        indexResponse.getMappings().forEach((k, v) -> System.out.println(v.getSourceAsMap()));
    }

    @Test
    public void testUpdateIndex() {
        // 测试更新索引
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 指定要更新哪个索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        wrapper.indexName(indexName);
        wrapper.mapping(Document::getCreator, FieldType.KEYWORD);
        wrapper.mapping(Document::getGmtCreate, FieldType.DATE);
        boolean isOk = documentMapper.updateIndex(wrapper);
        Assertions.assertTrue(isOk);
    }

    @Test
    public void testDeleteIndex() {
        // 测试删除索引
        // 指定要删除哪个索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean isOk = documentMapper.deleteIndex(indexName);
        Assertions.assertTrue(isOk);
    }

    @Test
    public void testCreateIndexByMap() {
        // 演示通过自定义map创建索引,最为灵活,若我提供的创建索引API不能满足时可用此方法
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        wrapper.indexName(Document.class.getSimpleName().toLowerCase());
        wrapper.settings(3, 2);
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> prop = new HashMap<>();
        Map<String, String> field = new HashMap<>();
        field.put("type", FieldType.KEYWORD.getType());
        prop.put("this_is_field", field);
        map.put("properties", prop);
        wrapper.mapping(map);
        boolean isOk = documentMapper.createIndex(wrapper);
        Assertions.assertTrue(isOk);
    }

    @Test
    public void testActiveIndex(){
        String indexName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        documentMapper.setCurrentActiveIndex(indexName);
    }
}
