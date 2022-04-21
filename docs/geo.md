> 支持版本:0.9.5+
> 地理位置查询,与Es官方提供的功能完全一致,共支持4种类型的地理位置查询:
> - GeoBoundingBox
> - GeoDistance
> - GeoPolygon
> - GeoShape
> 
通过这4类查询,可以实现各种强大实用的功能

应用场景:

- 外卖类APP 附近的门店
- 社交类APP 附近的人
- 打车类APP 附近的司机
- 区域人群画像类APP 指定范围内的人群特征提取
- 健康码等
- ...

功能覆盖100%,且使用更为简单,4类查询的详细介绍请点击左侧菜单进入子项进行查看
> **Tips:**
> 1. 在使用地理位置查询API之前,需要提前创建或更新好索引
> - 其中前三类API(GeoBoundingBox,GeoDistance,GeoPolygon)字段索引类型必须为geo_point
> - GeoShape字段索引类型必须为geo_shape,具体可参考下图
> 
> 2. 字段类型推荐使用String,因为wkt文本格式就是String,非常方便,至于字段名称,见名知意即可.
> 

![1](https://iknow.hs.net/94fcefcc-3bfd-48c6-99fa-2bfa6d803f20.png)
```java
public class Document {
	// 省略其它字段...
	private String location;
    private String geoLocation;
}
```
