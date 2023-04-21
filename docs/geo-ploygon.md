GeoPolygon:直译为地理多边形,实际上就是以给定的所有点构成的多边形为范围,查询此范围内的所有点,此功能常被用来做电子围栏,使用也较为高频,像共享单车可以停放的区域就可以通过此技术实现,可参考下图:

![1](https://iknow.hs.net/8c72431f-a5e7-48da-9021-c25dc0adc081.png)

API:
```java
geoPolygon(R column, List<GeoPoint> geoPoints)
```
使用示例:
```java
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
```
> **Tips:**
> 1. 同样的,关于坐标点的入参形式,也支持多种,与官方一致,可以参考GeoBoundingBox中的Tips,这里不赘述.值得注意的是多边形的点数不能少于3个,否则Es无法勾勒出多边形,本次查询会报错.
> 1. 索引类型和字段类型与GeoBondingBox中的Tips介绍的一样


