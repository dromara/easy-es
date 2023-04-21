GeoShape:直译为地理图形,怎么理解?乍一看好像和GeoPolygon很像,但实际上,前面三种类型查询的都是坐标点,而此方法查询的是图形,比如一个园区,从世界地图上看可以把它当做一个点,但如果放得足够大,比如把地图具体到杭州市滨江区,园区就可能变成若干个点构成的一个区域,在一些特殊的场景中,需要查询此完整的区域,以及两个区域的交集之类的,就需要用到GeoShape了,如果你还不理解,不妨先接着往下看,以杭州为例,我举一个健康码的例子,假设黑色圈内区域为中风险地区,我现在要查出ES中所有在市民中心且处于中风险区域的人,把他们的健康码统统变成橙色,那实际上我要找的就是下图中橙色那块区域,此时红色箭头所构成的区域是整个市民中心,我可以把整个市民中心作为一个地理图形,然后把黑色大圆作为查询的图形,找出它们的交集即可.

![1](https://iknow.hs.net/0160ab8d-ac6b-4c6a-b438-bf5da8cd0d34.png)

上图对应的ShapeRelation为INTERSECTS,看以看下面API.

API:
```java
// 查询符合已索引图形的图形
geoShape(R column, String indexedShapeId);

// 查询不符合已索引图形的图形 (0.9.7+ 版本支持)
notInGeoShape(R column, String indexedShapeId);

// 查询符合指定图形和图形关系的图形列表
geoShape(R column, Geometry geometry, ShapeRelation shapeRelation);

// 查询不符合指定图形和图形关系的图形列表
notInGeoShape(R column, Geometry geometry, ShapeRelation shapeRelation);
```
使用示例:

此API不常用,也可直接跳过看下面通过图形查询的.
```java
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

        // 不符合的情况
        // wrapper.notInGeoShape(Document::getGeoLocation, "edu");

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
此API相较上面方式更常用,即用户可以自行指定要查询的图形是矩形,圆形,还是多边形...(具体看代码中注释):
```java
 /**
     * 图形由用户自定义(常用),本框架支持Es所有支持的图形:
     * (Point,MultiPoint,Line,MultiLine,Circle,LineaRing,Polygon,MultiPolygon,Rectangle)
     */
    @Test
    public void testGeoShape() {
        // 注意,这里查询的是图形,所以图形的字段索引类型必须为geoShape,不能为geoPoint,故这里用geoLocation字段而非location字段
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 这里以矩形为例演示,其中x,y为圆心坐标,r为半径. 其它图形请读者自行演示,篇幅原因不一一演示了
        Circle circle = new Circle(13,14,100);
        // shapeRelation支持多种,如果不传则默认为within
        wrapper.geoShape(Document::getGeoLocation, circle, ShapeRelation.INTERSECTS);

        // 不符合的情况
        // wrapper.notInGeoShape(Document::getGeoLocation, circle, ShapeRelation.INTERSECTS);

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
上述地图中的
市民中心(多边形)的WKT(Well-Known Text)坐标(模拟的数据,真实数据可从高德地图/百度地图等通过 调用它们提供的开放API获取):

"POLYGON((108.36549282073975 22.797566864832092,108.35974216461182 22.786093175673713,108.37265968322754 22.775963875498206,108.4035587310791 22.77600344454008,108.41003894805907 22.787557113881462,108.39557647705077 22.805360509802284,108.36549282073975 22.797566864832092))";

已经存储在Es中了,实际上我们在项目中都会把可能用到的数据或业务数据都事先存入Es了,否则查询也就无意义了,查个空气? 所以上面API根据GeoShape查询时,需要传入的参数的仅是你圈定的范围的图形(上面该参数是圆).

> **TIps:**
> - GeoShape容易和GeoPolygon混淆,需要特别注意,它俩其实是两种不同的东西.
> - GeoShape进行查询的字段索引类型必须是geo_shape类型,否则此API无法使用,参考下图
> - 字段类型推荐使用String,因为wkt文本格式就是String,非常方便,至于字段名称,见名知意即可.

![2](https://iknow.hs.net/17915c0a-151e-497b-bd0f-5f70868d35a6.png)
```java
public class Document {
	// 省略其它字段...
	private String geoLocation;
}
```
