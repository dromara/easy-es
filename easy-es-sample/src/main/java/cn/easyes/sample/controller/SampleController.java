package cn.easyes.sample.controller;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.sample.entity.Document;
import cn.easyes.sample.mapper.DocumentMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 演示springboot项目实际使用
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RestController
public class SampleController {

    @Resource
    private DocumentMapper documentMapper;

    /**
     * 初始化插入数据,默认开启自动挡,自动挡模式下,索引会自动创建及更新. 若未开启自动挡,则在此步骤前需先调用创建索引API完成索引创建
     *
     * @return
     */
    @GetMapping("insert")
    public Integer insert() {
        Document document = new Document();
        document.setId("1");
        document.setTitle("测试1");
        document.setContent("测试内容1");
        document.setCreator("老汉");
        document.setGmtCreate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return documentMapper.insert(document);
    }

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


    /**
     * 演示根据title删除文章，同时会被 DeleteInterceptor 拦截，执行逻辑删除
     *
     * @param title
     * @return
     */
    @GetMapping("/deleteDocumentByTitle")
    public Integer deleteDocumentByTitle(@RequestParam String title) {
        // 实际开发中会把这些逻辑写进service层 这里为了演示方便就不创建service层了
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, title);
        return documentMapper.delete(wrapper);
    }

    /**
     * 自定义注解指定高亮返回字段,高亮查询测试
     *
     * @param content
     * @return
     */
    @GetMapping("/highlightSearch")
    public List<Document> highlightSearch(@RequestParam String content) {
        // 实际开发中会把这些逻辑写进service层 这里为了演示方便就不创建service层了
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, content);
        return documentMapper.selectList(wrapper);
    }
}
