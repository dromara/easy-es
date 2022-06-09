package cn.easyes.test.geo;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Circle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public void testGeoBoundingBox() {
        // 查询位于下面左上点和右下点坐标构成的长方形内的所有点
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 左上点坐标
        GeoPoint leftTop = new GeoPoint(41.187328D, 115.498353D);
        // 右下点坐标
        GeoPoint bottomRight = new GeoPoint(39.084509D, 117.610461D);
        wrapper.geoBoundingBox(Document::getLocation, leftTop, bottomRight);
        // 查不在此长方形内的所有点
//         wrapper.notInGeoBoundingBox(Document::getLocation, leftTop, bottomRight);
        List<Document> documents = documentMapper.selectList(wrapper);
        documents.forEach(System.out::println);
    }

    @Test
    public void testGeoDistance() {
        // 查询以经度为41.0,纬度为115.0为圆心,半径168.8公里内的所有点
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.geoDistance(Document::getLocation, 168.8, DistanceUnit.KILOMETERS, new GeoPoint(41.0, 116.0));
        // 上面语法也可以写成下面这几种形式,效果是一样的,兼容不同用户习惯而已:
//        wrapper.geoDistance(Document::getLocation,"1.5km",new GeoPoint(41.0,115.0));
//        wrapper.geoDistance(Document::getLocation, "1.5km", "41.0,115.0");

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testGeoPolygon() {
        // 查询以给定点列表构成的不规则图形内的所有点,点数至少为3个
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        List<GeoPoint> geoPoints = new ArrayList<>();
        GeoPoint geoPoint = new GeoPoint(40.178012, 116.577188);
        GeoPoint geoPoint1 = new GeoPoint(40.169329, 116.586315);
        GeoPoint geoPoint2 = new GeoPoint(40.178288, 116.591813);
        geoPoints.add(geoPoint);
        geoPoints.add(geoPoint1);
        geoPoints.add(geoPoint2);
        wrapper.geoPolygon(Document::getLocation, geoPoints);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    /**
     * 已知图形索引ID(不常用)
     * 在一些高频场景下,比如一个已经造好的园区,其图形坐标是固定的,因此可以先把这种固定的图形先存进es
     * 后续可根据此图形的id直接查询,比较方便,故有此方法,但不够灵活,不常用
     */
    @Test
    public void testGeoShapeWithShapeId() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 这里的indexedShapeId为用户事先已经在Es中创建好的图形的id
        wrapper.geoShape(Document::getGeoLocation, "edu");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    /**
     * 图形由用户自定义(常用),本框架支持Es所有支持的图形:
     * (Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     */
    @Test
    public void testGeoShape() {
        // 注意,这里查询的是图形,所以图形的字段索引类型必须为geoShape,不能为geoPoint,故这里用geoLocation字段而非location字段
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 这里以矩形为例演示,其中x,y为圆心坐标,r为半径. 其它图形请读者自行演示,篇幅原因不一一演示了
        Circle circle = new Circle(13, 14, 100);
        // shapeRelation支持多种,如果不传则默认为within
        wrapper.geoShape(Document::getGeoLocation, circle, ShapeRelation.INTERSECTS);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
}
