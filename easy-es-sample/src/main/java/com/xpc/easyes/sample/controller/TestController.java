package com.xpc.easyes.sample.controller;

import com.xpc.easyes.core.conditions.LambdaEsIndexWrapper;
import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 演示实际使用
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RestController
public class TestController {
    @Resource
    private DocumentMapper documentMapper;

    /**
     * 演示根据标题精确查询文章
     * 例如title传值为 我真帅,那么在当前配置的索引下,所有标题为'我真帅'的文章都会被查询出来
     * 其它各种场景的查询使用,请移步至test模块
     *
     * @param title
     * @return
     */
    @GetMapping("/listDocumentByTitle")
    public List<Document> listDocumentByTitle(@RequestParam String title) {
        // 实际开发中会把这些逻辑写进service层 这里为了演示方便就不创建service层了
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, title);
        return documentMapper.selectList(wrapper);
    }

    @GetMapping("/index")
    public Boolean index() {
        // 初始化-> 创建索引,相当于MySQL建表 | 此接口须首先调用,只调用一次即可
        LambdaEsIndexWrapper<Document> indexWrapper = new LambdaEsIndexWrapper<>();
        indexWrapper.indexName(Document.class.getSimpleName().toLowerCase());
        indexWrapper.mapping(Document::getTitle, FieldType.KEYWORD)
                .mapping(Document::getContent, FieldType.TEXT);
        documentMapper.createIndex(indexWrapper);
        return Boolean.TRUE;
    }



    /**
     * 高亮查询测试
     * @param content
     * @return
     */
    @GetMapping("/highlightSearch")
    public List<Document> highlightSearch(@RequestParam String content) {
        // 实际开发中会把这些逻辑写进service层 这里为了演示方便就不创建service层了
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, content).highLight(Document::getContent);
        return documentMapper.selectList(wrapper);
    }


    /**
     * 测试数据
     * @return
     */
    @GetMapping("batchSave")
    public Boolean batchSave() {
        List<Document> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Document document = new Document();
            document.setTitle("标题"+i)
                    .setContent("这是一个内容  哈哈 "+i);
            list.add(document);
        }
        documentMapper.insertBatch(list);
        return true;
    }

}
