package com.xpc.easyes.sample.test.delete;

import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.sample.EasyEsApplication;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 删除测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class DeleteTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testDelete() {
        // 测试删除数据 删除有两种情况:根据id删或根据条件删
        // 鉴于根据id删过于简单,这里仅演示根据条件删,以老李的名义删,让老李心理平衡些
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "小伙子";
        wrapper.eq(Document::getTitle, title);
        int successCount = documentMapper.delete(wrapper);
        System.out.println(successCount);
    }

    @Test
    public void testDeleteBatchIds() throws IOException {
//        List<String> ids = Arrays.asList("nsi4T30BUP1SGuceaaTf","n8i4T30BUP1SGuceaaTf","oMi4T30BUP1SGuceaaTf");
//        int successCount = documentMapper.deleteBatchIds(ids);
//        System.out.println(successCount);
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.isNotNull(Document::getTitle)
                .and(w -> w.match(Document::getCustomField, "乌拉").or().eq(Document::getCustomField, "魔鬼"));
        int successCount = documentMapper.delete(wrapper);
        System.out.println(successCount);
    }
}
