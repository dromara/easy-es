GeoBoundingBox: Literally translated as a geographic bounding box, a rectangular range formed by the upper left point and the lower right point, all points within this range can be queried, but not many are actually used, please refer to the following figure:

![1](https://iknow.hs.net/c3703152-b379-4394-8c24-ccfd08f5981b.png)

API:
```java
geoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight)

// not inside a rectangle (supported in version 0.9.7+)
notInGeoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight); 
```
Example of use:
```java
@Test
    public void testGeoBoundingBox() {
        // Query all points within the rectangle formed by the coordinates of the upper left and lower right points
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint leftTop = new GeoPoint(41.187328D, 115.498353D);
        GeoPoint bottomRight = new GeoPoint(39.084509D, 117.610461D);
        wrapper.geoBoundingBox(Document::getLocation, leftTop, bottomRight);
        List<Document> documents = documentMapper.selectList(wrapper);
        documents.forEach(System.out::println);
    }
```
> **Tips:**
> 1. The above example only demonstrates one of them. In fact, there are many syntax support for coordinate points in this framework. Several data formats officially provided by ElasticSearch are supported. Users can choose the corresponding api according to their own habits to construct query parameters:
> - **GeoPoint:** The latitude and longitude representation used in the demo above
> - **Longitude and latitude array:**[116.498353, 40.187328],[116.610461, 40.084509]
> - **Latitude and longitude strings:** "40.187328, 116.498353", "116.610461, 40.084509
> - **Latitude and longitude bounding box WKT: **"BBOX (116.498353,116.610461,40.187328,40.084509)"
> - **Latitude and longitude GeoHash (hash):** "xxx"
> 
     Among them, the conversion of latitude and longitude hash can refer to this website: [GeoHash Coordinate Online Conversion](http://geohash.co/)
> 
> 2. The index type of the field must be geoPoint, otherwise the related query API will fail, please refer to the following figure.
> 2. The field type is recommended to use String, because the wkt text format is String, which is very convenient. As for the field name, you can see the name.

![](https://iknow.hs.net/90dde93d-653d-4973-9d6f-4068d625f396.png)

```java
public class Document {
	// Omit other fields ...
	private String location;
}
```
