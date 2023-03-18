package cn.easyes.test.geo;

import cn.easyes.core.conditions.select.LambdaEsQueryWrapper;
import cn.easyes.test.TestEasyEsApplication;
import cn.easyes.test.entity.Document;
import cn.easyes.test.mapper.DocumentMapper;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Circle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Geo测试
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Disabled
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
        // 这里以圆形为例演示,其中x,y为圆心坐标,r为半径. 其它图形请读者自行演示,篇幅原因不一一演示了
        Circle circle = new Circle(13, 14, 100);
        // shapeRelation支持多种,如果不传则默认为within
        wrapper.geoShape(Document::getGeoLocation, circle, ShapeRelation.INTERSECTS);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testOrderByDistance() {
        // 1.数据准备与入库,经纬度数据来源于腾讯地图

        // 长沙站
        Document centerDoc = new Document();
        centerDoc.setEsId("1");
        centerDoc.setTitle("长沙站");
        centerDoc.setContent("长沙站臭豆腐店");
        GeoPoint center = new GeoPoint(28.194124244622135, 113.01327520919799);
        centerDoc.setLocation(center.toString());

        // 长沙火车站
        Document document1 = new Document();
        document1.setEsId("2");
        document1.setTitle("长沙火车站");
        document1.setContent("长沙火车站臭豆腐店");
        GeoPoint geoPoint1 = new GeoPoint(28.193708185086585, 113.01100069595336);
        document1.setLocation(geoPoint1.toString());

        // 长沙站南广场
        Document document2 = new Document();
        document2.setEsId("3");
        document2.setTitle("长沙站南广场");
        document2.setContent("长沙站南广场臭豆腐店");
        GeoPoint geoPoint2 = new GeoPoint(28.192195227668382, 113.01173025680541);
        document2.setLocation(geoPoint2.toString());


        // 长沙市中医院(东院区)
        Document document3 = new Document();
        document3.setEsId("4");
        document3.setTitle("长沙市中医院");
        document3.setContent("长沙市中医院臭豆腐店");
        GeoPoint geoPoint3 = new GeoPoint(28.193367771534916, 113.00911242080687);
        document3.setLocation(geoPoint3.toString());

        // 阿波罗商业广场
        Document document4 = new Document();
        document4.setEsId("5");
        document4.setTitle("阿波罗商业广场");
        document4.setContent("阿波罗商业广场臭豆腐店");
        GeoPoint geoPoint4 = new GeoPoint(28.196582745180983, 113.00962740493773);
        document4.setLocation(geoPoint4.toString());

        // 朝阳一村
        Document document5 = new Document();
        document5.setEsId("6");
        document5.setTitle("朝阳一村");
        document5.setContent("朝阳一村臭豆腐店");
        GeoPoint geoPoint5 = new GeoPoint(28.188980122051586, 113.01177317214965);
        document5.setLocation(geoPoint5.toString());

        // 铭威大厦
        Document document6 = new Document();
        document6.setEsId("7");
        document6.setTitle("铭威大厦");
        document6.setContent("铭威大厦臭豆腐店");
        GeoPoint geoPoint6 = new GeoPoint(28.1905309497745, 113.01825338912963);
        document6.setLocation(geoPoint6.toString());

        // 袁家岭
        Document document7 = new Document();
        document7.setEsId("8");
        document7.setTitle("袁家岭");
        document7.setContent("袁家岭豆腐店");
        GeoPoint geoPoint7 = new GeoPoint(28.19450247915946, 113.00070101333617);
        document7.setLocation(geoPoint7.toString());

        List<Document> documents = Arrays.asList(centerDoc, document1, document2, document3, document4, document5, document6, document7);
        int count = documentMapper.insertBatch(documents);
        Assertions.assertEquals(8, count);

        // 2.等待数据在所有分片处理完毕
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3.执行排序,并校验排序结果是否符合预期

        // case1: 查询出距离中心点长沙站5公里内最近的臭豆腐店,按由近及远排序
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "臭豆腐")
                .geoDistance(Document::getLocation, 5.0, center)
                .orderByDistanceAsc(Document::getLocation, center);
        List<Document> resultList = documentMapper.selectList(wrapper);
        System.out.println(resultList);

        // 除了袁家岭,其它数据均在半径内5km内,所以期望有7条数据命中,且最近的为中心点长沙站,最远的为铭威大厦
        Assertions.assertEquals(7, resultList.size());
        Assertions.assertEquals("长沙站", resultList.get(0).getTitle());
        Assertions.assertEquals("长沙火车站", resultList.get(1).getTitle());
        Assertions.assertEquals("铭威大厦", resultList.get(6).getTitle());
        // 最远的距离-最近的距离大于0
        Assertions.assertTrue(resultList.get(6).getDistance() - resultList.get(0).getDistance() > 0);

        // case2: 查询出距离中心点长沙站最远的臭豆腐店,按由远及远排序
        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.match(Document::getContent, "臭豆腐")
                .geoDistance(Document::getLocation, 5.0, center)
                .orderByDistanceDesc(Document::getLocation, center);
        List<Document> resultList1 = documentMapper.selectList(wrapper1);
        System.out.println(resultList1);

        // 除了袁家岭,其它数据均在半径内5km内,所以期望有7条数据命中,且排在首位的是最远的为铭威大厦,末位的是最近的中心点长沙站
        Assertions.assertEquals(7, resultList1.size());
        Assertions.assertEquals("铭威大厦", resultList1.get(0).getTitle());
        Assertions.assertEquals("朝阳一村", resultList1.get(1).getTitle());
        Assertions.assertEquals("长沙站", resultList1.get(6).getTitle());

        // 最远的距离-最近的距离大于0
        Assertions.assertTrue(resultList1.get(0).getDistance() - resultList1.get(6).getDistance() > 0);
    }
}
