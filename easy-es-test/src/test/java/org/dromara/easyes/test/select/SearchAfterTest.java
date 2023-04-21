package org.dromara.easyes.test.select;


import org.dromara.easyes.core.biz.SAPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.core.EsWrappers;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * searchAfter测试
 **/
@Disabled
@SpringBootTest(classes = TestEasyEsApplication.class)
public class SearchAfterTest {

    @Resource
    private DocumentMapper documentMapper;

    @Test
    void error() {
        LambdaEsQueryWrapper<Document> lambdaEsQueryWrapper = EsWrappers.lambdaQuery(Document.class);
        lambdaEsQueryWrapper.size(10);
        lambdaEsQueryWrapper.orderByDesc(Document::getEsId, Document::getStarNum);
        //重现bug需要注释掉searchAfter中对from的校验
//        lambdaEsQueryWrapper.from(10);
        SAPageInfo<Document> saPageInfo = documentMapper.searchAfterPage(lambdaEsQueryWrapper, null, 10);
        //第一页
        System.out.println(saPageInfo);
        Assertions.assertEquals(10, saPageInfo.getList().size());
    }

}
