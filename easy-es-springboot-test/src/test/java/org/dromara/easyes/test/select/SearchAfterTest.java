package org.dromara.easyes.test.select;


import org.dromara.easyes.core.biz.SAPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.kernel.EsWrappers;
import org.dromara.easyes.test.TestEasyEsApplication;
import org.dromara.easyes.test.entity.Document;
import org.dromara.easyes.test.mapper.DocumentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

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

    @Test
    public void test1() {
        documentMapper.createIndex();
        for (int i = 0; i < 30; i++) {
            Document document = new Document();
            document.setEsId(String.valueOf(i));
            document.setTitle("测试标题" + i);
            document.setContent("测试内容" + i);
            documentMapper.insert(document);
        }
    }

    @Test
    public void test2() {
        LambdaEsQueryWrapper<Document> wrapper = EsWrappers.lambdaQuery(Document.class);
        wrapper.match(Document::getContent, "测试");
        wrapper.orderByDesc(Document::getEsId);

        // 第一页,可传null,查完把saPageInfo返回给前端
        SAPageInfo<Document> saPageInfo = documentMapper.searchAfterPage(wrapper, null, 10);
        System.out.println(saPageInfo);

        // 第二页,从saPageInfo中把上一次的nextSearchAfter回传给后端
        List<Object> nextSearchAfter = saPageInfo.getNextSearchAfter();
        SAPageInfo<Document> saPageInfo1 = documentMapper.searchAfterPage(wrapper, nextSearchAfter, 10);
        System.out.println(saPageInfo1);
    }

}
