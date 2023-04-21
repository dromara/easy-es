package org.dromara.easyes.test.update;

import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class UpdateTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testUpdate() {
        // 测试更新 更新有两种情况 分别演示如下:
        // case1: 已知id, 根据id更新 (为了演示方便,此id是从上一步查询中复制过来的,实际业务可以自行查询)
        String id = "5";
        String title1 = "隔壁老王";
        Document document1 = new Document();
        document1.setEsId(id);
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

        // 关于case2 另一种省略实体的简单写法,语法与MP一致
        LambdaEsUpdateWrapper<Document> wrapper3 = new LambdaEsUpdateWrapper<>();
        wrapper3.eq(Document::getTitle, title1);
        wrapper3.set(Document::getContent,"推*技术过软")
                .set(Document::getCustomField,"乌拉巴拉中魔仙");
        documentMapper.update(null,wrapper);
    }

    @Test
    public void testBatchUpdateByIds() {
        List<Document> documentList = new ArrayList<>();

        Document document = new Document();
        document.setEsId("O2EQCIAB0E2Rzy0qHFNV");
        document.setTitle("老王ba");
        document.setContent("推*技术过硬???");
        document.setCreator("老王ba");

        Document document1 = new Document();
        document1.setEsId("OmEQCIAB0E2Rzy0qHFNV");
        document1.setTitle("老李ba");
        document1.setContent("推*技术过硬???");
        document1.setCreator("老汉ba");

        documentList.add(document);
        documentList.add(document1);
        int successCount = documentMapper.updateBatchByIds(documentList);
        System.out.println(successCount);
    }
}
