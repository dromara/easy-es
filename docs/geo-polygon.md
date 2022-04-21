GeoPolygon:直译为地理多边形,实际上就是以给定的所有点构成的多边形为范围,查询此范围内的所有点,此功能常被用来做电子围栏,使用也较为高频,像共享单车可以停放的区域就可以通过此技术实现,可参考下图:<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/21559896/1645002482721-3a0853c7-dad9-4891-8317-9d28bd536f0a.png#clientId=u4267ab6a-28ce-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=170&id=u82b63a52&margin=%5Bobject%20Object%5D&name=image.png&originHeight=170&originWidth=249&originalType=binary&ratio=1&rotation=0&showTitle=false&size=60736&status=done&style=none&taskId=u1d95d530-4174-4207-aac1-da27c80da21&title=&width=249)<br />API:
```java
// 查询在多边形内的所有点
geoPolygon(R column, List<GeoPoint> geoPoints);

// 查询不在多边形内的所有点(0.9.7+ 版本支持)
notInGeoPolygon(R column, List<GeoPoint> geoPoints);
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

        // 查询不在多边形内的所有点
        // wrapper.notInGeoPolygon(Document::getLocation, geoPoints);

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
> **Tips:**
> 1. 同样的,关于坐标点的入参形式,也支持多种,与官方一致,可以参考GeoBoundingBox中的Tips,这里不赘述.值得注意的是多边形的点数不能少于3个,否则Es无法勾勒出多边形,本次查询会报错.
> 1. 索引类型和字段类型与GeoBondingBox中的Tips介绍的一样


