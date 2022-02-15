package com.xpc.easyes.sample.test.geo;

import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.sample.entity.Document;
import com.xpc.easyes.sample.mapper.DocumentMapper;
import com.xpc.easyes.sample.test.TestEasyEsApplication;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Geo测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestEasyEsApplication.class)
public class GeoTest {
    @Resource
    private DocumentMapper documentMapper;

    @Test
    public void testGeoBoundingBox(){
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 左上点坐标
        GeoPoint leftTop = new GeoPoint(41.187328D,115.498353D);
        // 右下点坐标
        GeoPoint bottomRight = new GeoPoint(39.084509D,117.610461D);
        wrapper.eq(Document::getTitle,"老汉");
        wrapper.geoBoundingBox(Document::getLocation,leftTop,bottomRight);
        String source = documentMapper.getSource(wrapper);
        System.out.println(source);
        List<Document> documents = documentMapper.selectList(wrapper);
        documents.forEach(System.out::println);
    }
}
