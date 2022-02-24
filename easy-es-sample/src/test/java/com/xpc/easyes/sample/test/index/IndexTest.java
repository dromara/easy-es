package com.xpc.easyes.sample.test.index;

import com.xpc.easyes.core.conditions.LambdaEsIndexWrapper;
import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 索引测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class IndexTest {
    @Resource
    private DocumentMapper documentMapper;

    /**
     * 测试创建索引 不了解Es索引概念的建议先去了解 懒汉可以简单理解为MySQL中的一张表
     */
    @Test
    public void testCreatIndex() {
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 此处简单起见 索引名称须保持和实体类名称一致,字母小写 后面章节会教大家更如何灵活配置和使用索引
        wrapper.indexName(Document.class.getSimpleName().toLowerCase());

        // 此处将文章标题映射为keyword类型(不支持分词),文档内容映射为text类型(支持分词查询)
        wrapper.mapping(Document::getTitle, FieldType.KEYWORD)
                .mapping(Document::getContent,FieldType.TEXT)
                .mapping(Document::getLocation,FieldType.GEO_POINT)
                .mapping(Document::getGeoLocation,FieldType.GEO_SHAPE)
                .mapping(Document::getContent, FieldType.TEXT,Analyzer.IK_SMART,Analyzer.IK_SMART);

        // 设置分片及副本信息,可缺省
        wrapper.settings(3, 2);

        // 设置别名信息,可缺省
        String aliasName = "daily";
        wrapper.createAlias(aliasName);

        // 创建索引
        boolean isOk = documentMapper.createIndex(wrapper);
        Assert.assertTrue(isOk);
    }

    @Test
    public void testExistsIndex() {
        // 测试是否存在指定名称的索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean existsIndex = documentMapper.existsIndex(indexName);
        Assert.assertTrue(existsIndex);
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
        Assert.assertTrue(isOk);
    }

    @Test
    public void testDeleteIndex() {
        // 测试删除索引
        // 指定要删除哪个索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean isOk = documentMapper.deleteIndex(indexName);
        Assert.assertTrue(isOk);
    }
}
