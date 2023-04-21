GeoDistance: Literally translated as geographic distance, in fact, it takes a given point as the center, draws a circle with a given radius, and the points within this circle can be detected and used more frequently, such as the takeaway we use Software, you can use this function to query all the stores within 3 kilometers around, yes, you can also use it to write social software to query the beautiful women within 3 kilometers nearby...

![1](https://iknow.hs.net/b979de3a-3130-4c42-be01-8cf74435e9c8.png)

```java
geoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint)

// Find all points not inside a circle (supported in version 0.9.7+)
notInGeoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint);    
```
```java
    @Test
    public void testGeoDistance() {
        // Query all points within a radius of 168.8 kilometers with a longitude of 41.0 and a latitude of 115.0 as the center and a radius of 168.8 kilometers.
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // The unit can be omitted, the default is km
        wrapper.geoDistance(Document::getLocation, 168.8, DistanceUnit.KILOMETERS, new GeoPoint(41.0, 116.0));
        // The above syntax can also be written in the following forms, the effect is the same, and it is compatible with different user habits:
//        wrapper.geoDistance(Document::getLocation,"1.5km",new GeoPoint(41.0,115.0));
//        wrapper.geoDistance(Document::getLocation, "1.5km", "41.0,115.0");

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
> Tips:
> 1. The same form of expression for coordinate points is also supported, which is the same as the Tips in GeoBondingBox, and will not be repeated here.
> 1. The index type and field type are the same as those described in the Tips in GeoBondingBox
> 1. For fans of EE, it is a matter of course to be compatible with the different habits of various users, so you will find a lot of method overloading when you use it. Choose an API that best suits your usage habits or the specified usage scenario to call. .

