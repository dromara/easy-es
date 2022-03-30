package com.xpc.easyes.sample.test.insert;

import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.elasticsearch.geometry.Point;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 插入测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class InsertTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testInsert() {
        // 测试插入数据
        Document document = new Document();
        document.setTitle("老汉");
        document.setContent("推*技术过硬");
        document.setCreator("老汉");
        document.setLocation("40.171975,116.587105");
        Point point = new Point(13.400544, 52.530286);
        document.setGeoLocation(point.toString());
        int successCount = documentMapper.insert(document);
        Assert.assertEquals(successCount, 1);
    }

    @Test
    public void testInsertBatch() {
        List<Document> documentList = new ArrayList<>();
        Document document = new Document();
        document.setTitle("老王");
        document.setContent("推*技术过硬");
        document.setCreator("老王");
        document.setLocation("40.17836693398477,116.64002551005981");

        Document document1 = new Document();
        document1.setTitle("老李");
        document1.setContent("推*技术过硬");
        document1.setCreator("老汉");
        document1.setLocation("40.19103839805197,116.5624013764374");


        Document document2 = new Document();
        document2.setTitle("老汉");
        document2.setContent("推*技术过硬");
        document2.setCreator("老汉");
        document2.setLocation("40.13933715136454,116.63441990026217");

        documentList.add(document);
        documentList.add(document1);
        documentList.add(document2);

        int successCount = documentMapper.insertBatch(documentList);
        System.out.println(successCount);
        // id可以直接从被插入的数据中取出
        documentList.forEach(System.out::println);
    }

    @Test
    public void testInsertWithCustomizeId() {
        // 测试用户自行指定id新增,测试前必须先把注解@TableId中的type指定为IdType.CUSTOMIZE 或在配置文件yml中指定
        String id = "muscle";
        Document document = new Document();
        document.setId(id);
        document.setTitle("测试用户自定义id");
        document.setContent("测试用户自己指定id,如果es中已存在该id就更新该数据,不存在时才新增");
        int successCount = documentMapper.insert(document);
        System.out.println(successCount);

        // id保持不变,只改内容,会自动更新此id对应的数据 此方法是Insert,所以成功条目数返回为0
        document.setTitle("我被更新了1");
        documentMapper.insert(document);
        Document document1 = documentMapper.selectById(id);
        System.out.println("更新后的数据:" + document1);
    }
}
