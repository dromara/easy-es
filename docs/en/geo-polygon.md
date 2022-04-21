GeoPolygon: Literally translated as geographic polygon, in fact, it takes the polygon formed by all the given points as the range, and queries all the points within this range. This function is often used for electronic fences, and it is used more frequently, such as sharing bicycles. The parking area can be realized by this technology, please refer to the following figure:

![1](https://iknow.hs.net/b979de3a-3130-4c42-be01-8cf74435e9c8.png)

API:
```java
geoPolygon(R column, List<GeoPoint> geoPoints)

// Query all points not inside a polygon (supported in version 0.9.7+)
notInGeoPolygon(R column, List<GeoPoint> geoPoints);
```
Example of use:
```java
    @Test
    public void testGeoPolygon() {
        // Query all points in an irregular graph composed of a given list of points, the number of points is at least 3
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
> 1. Similarly, regarding the input parameter form of the coordinate point, it also supports a variety of forms. It is consistent with the official. You can refer to the Tips in GeoBoundingBox, which will not be repeated here. It is worth noting that the number of points in the polygon cannot be less than 3, otherwise Es cannot outline the polygon. , this query will report an error.
> 1. The index type and field type are the same as those described in the Tips in GeoBondingBox

