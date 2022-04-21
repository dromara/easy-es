GeoShape: Literally translated as geographic graphics, how do you understand it? At first glance, it seems to be very similar to GeoPolygon, but in fact, the first three types of queries are all coordinate points, and this method queries graphics, such as a park, from the world map. It can be regarded as a point, but if the map is made large enough, for example, the map is specific to Binjiang District, Hangzhou, the park may become an area composed of several points. In some special scenarios, you need to query this complete Area, and the intersection of two areas, etc., you need to use GeoShape. If you don’t understand it, you might as well read it down first. Take Hangzhou as an example. I will give an example of a health code, assuming the area inside the black circle. For the medium-risk area, I now want to find out all the people in the ES who are in the civic center and in the medium-risk area, and turn all their health codes into orange. In fact, what I am looking for is the orange area in the picture below. At this time, the area formed by the red arrow is the entire civic center. I can use the entire civic center as a geographic graph, and then use the large black circle as the query graph to find their intersection.

![1](https://iknow.hs.net/9f1e6b34-073e-428c-8c7f-86a6bc82d243.png)

The ShapeRelation corresponding to the above figure is INTERSECTS, take a look at the following API.

API:
```java
geoShape(R column, String indexedShapeId);

// Querying graphs that do not conform to indexed graphs (supported in version 0.9.7+)
notInGeoShape(R column, String indexedShapeId);

geoShape(R column, Geometry geometry, ShapeRelation shapeRelation);

// Query a list of graphs that do not match the specified graph and graph relationship
notInGeoShape(R column, Geometry geometry, ShapeRelation shapeRelation);
```
Example of use:<br />This API is not commonly used, you can also skip directly to see the query by graph below.
```java
	 /**
      * Known graphics index ID (not commonly used)
      * In some high-frequency scenarios, such as a park that has already been built, the graphics coordinates are fixed, so this fixed graphics can be stored in es first
      * Follow-up can be directly queried based on the id of this graph, which is more convenient, so there is this method, but it is not flexible enough and not commonly used
      */
    @Test
    public void testGeoShapeWithShapeId() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // The indexedShapeId here is the id of the shape that the user has created in Es in advance
        wrapper.geoShape(Document::getGeoLocation, "edu");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
This API is more commonly used than the above method, that is, the user can specify whether the graph to be queried is a rectangle, a circle, or a polygon... (see the comments in the code for details):
```java
	 /**
      * Graphics are customized by users (commonly used), this framework supports all supported graphics of Es:
      * (Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
      */
    @Test
    public void testGeoShape() {
		// Note that the graph is queried here, so the field index type of the graph must be geoShape, not geoPoint, so the geoLocation field is used instead of the location field       
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>(); 
        Circle circle = new Circle(13,14,100);
        // shapeRelation supports multiple types, if not passed, it defaults to within
        wrapper.geoShape(Document::getGeoLocation, circle, ShapeRelation.INTERSECTS);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
in the above map

WKT (Well-Known Text) coordinates of the civic center (polygon) (simulated data, real data can be obtained from AutoNavi Map/Baidu Map, etc. by calling the open API provided by them): "POLYGON((108.36549282073975 22.797566864832092,108.35974216461182 22.786093175673713)

It has been stored in Es. In fact, we will store all the data or business data that may be used in the project in Es in advance. Otherwise, the query will be meaningless. Check the air?

So when the above API queries based on GeoShape, the parameter that needs to be passed in is only the graph of the range you delineate (the parameter above is a circle).
> **TIps:**
> - GeoShape is easy to be confused with GeoPolygon and needs special attention, they are actually two different things.
> - The field index type for GeoShape query must be geo_shape type, otherwise this API cannot be used, refer to the following figure
> - The field type is recommended to use String, because the wkt text format is String, which is very convenient. As for the field name, you can see the name.

![2](https://iknow.hs.net/17915c0a-151e-497b-bd0f-5f70868d35a6.png)

```java
public class Document {
	// Omit other fields ...
	private String geoLocation;
}
```
