package com.xpc.easyes.sample.test.update;

import com.xpc.easyes.core.conditions.LambdaEsUpdateWrapper;
import com.xpc.easyes.core.toolkit.EsWrappers;
import com.xpc.easyes.sample.EasyEsApplication;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新测试
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 更新测试
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EasyEsApplication.class)
public class UpdateTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testUpdate() {
        // 测试更新 更新有两种情况 分别演示如下:
        // case1: 已知id, 根据id更新 (为了演示方便,此id是从上一步查询中复制过来的,实际业务可以自行查询)
        String id = "OmEQCIAB0E2Rzy0qHFNV";
        String title1 = "隔壁老王";
        Document document1 = new Document();
        document1.setId(id);
        document1.setTitle(title1);
        document1.setCustomField("乌拉巴拉大魔仙");
        documentMapper.updateById(document1);

        // case2: id未知, 根据条件更新
        LambdaEsUpdateWrapper<Document> wrapper = new LambdaEsUpdateWrapper<>();
        wrapper.eq(Document::getTitle, title1);
        Document document2 = new Document();
        document2.setTitle("隔壁老王王");
        document2.setContent("推*技术过软");
        document2.setCustomField("乌拉巴拉中魔仙");
        documentMapper.update(document2, wrapper);

        // 关于case2 还有另一种省略实体的简单写法,这里不演示,后面章节有介绍,语法与MP一致
    }

    @Test
    public void testBatchUpdateByIds() {
        List<Document> documentList = new ArrayList<>();

        Document document = new Document();
        document.setId("O2EQCIAB0E2Rzy0qHFNV");
        document.setTitle("老王ba");
        document.setContent("推*技术过硬???");
        document.setCreator("老王ba");

        Document document1 = new Document();
        document1.setId("OmEQCIAB0E2Rzy0qHFNV");
        document1.setTitle("老李ba");
        document1.setContent("推*技术过硬???");
        document1.setCreator("老汉ba");

        documentList.add(document);
        documentList.add(document1);
        int successCount = documentMapper.updateBatchByIds(documentList);
        System.out.println(successCount);
    }
}
