> Supported Versions: 0.9.5+
> Geographical location query, which is exactly the same as the function provided by Es official, supports a total of 4 types of geographic location query:
> - GeoBoundingBox
> - GeoDistance
> - GeoPolygon
> - GeoShape
> 
Through these four types of queries, various powerful and practical functions can be realized

Application Scenario:

● Stores near takeaway apps

● People near social apps

● Drivers near taxi apps

● Crowd feature extraction within the specified range of regional crowd portrait APPs

● Health code, etc.

● ...

The function coverage is 100%, and the use is simpler. For the detailed introduction of the 4 types of queries, please click the left menu to enter the sub-items to view
> **Tips:**
> 1. Before using the geolocation query API, you need to create or update the index in advance
> - The first three types of API (GeoBoundingBox, GeoDistance, GeoPolygon) field index type must be geo_point
> - GeoShape field index type must be geo_shape, please refer to the following figure for details
> 
> 2.The field type is recommended to use String, because the wkt text format is String, which is very convenient. As for the field name, you can see the name.

![](https://iknow.hs.net/94fcefcc-3bfd-48c6-99fa-2bfa6d803f20.png)

```java
public class Document {
	// Omit other fields...
	private String location;
    private String geoLocation;
}
```
