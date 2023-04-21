> 为了减少开发者的额外学习负担,我们尽量保持了和MP几乎一致的语法,但为了避免歧义,仍有个别地方存在些许差异,无论如何,在你看完这些差异和原因后,你肯定也会赞同这种差异存在的必要性.


**1.命名差异**<br />为了区别MP的命名带来的歧义问题,以下三处命名中我们加了Es字母区别于MP:

|  | MP | EE | 差异原因 |
| --- | --- | --- | --- |
| 启动类注解 | @MapperScan("xxx") | @EsMapperScan("xxx") | 一个项目中可能会同时用到MP和EE,避免同一系统中同时引入同名注解时,需要加全路径区分 |
| 父类Mapper命名 | BaseMapper<T> | BaseEsMapper<T> | 一个项目中可能会同时用到MP和EE,避免继承时误继承到MP的Mapper |
| 条件构造器命名 | LambdaQueryWrapper | LambdaEsQueryWrapper | 一个项目中可能会同时用到MP和EE,避免错误创建条件构造器 |

**2.移除了Service**<br />MP中引入了Service层,但EE中并无Service层,因为我个人认为MP的Service层太重了,不够灵活,实际开发中基本不用,被很多人吐槽,所以EE中我直接去掉了Service层,在使用过程中你无需像MP那样继承ISevice,另外我把一些高频使用的service层封装的方法下沉到了mapper层,比如批量更新,批量新增等,大家可以在调用基类Mapper层中的方法时看到,灵活且不失优雅.

**3.方法差异**<br />▼ group by 聚合<br />在EE中使用groupBy方法时,调用查询接口必须使用获取原生返回内容,不能像MP中一样返回泛型T,这点是由于ES和MySQL的差导致的,所以需要特别注意
```java

LambdaEsUpdateWrapper<T> wrapper = new LambdaEsUpdateWrapper<>();
wrapper.groupBy(T::getField);

// MP语法
List<T> list = xxxMapper.selectList(wrapper);
// EE语法
SearchResponse response = xxxMapper.search(wrapper);   
```
因为Es会把聚合的结果单独放到aggregations对象中,但原来的实体对象中并无此字段,所以我们需要用SearchResponse接收查询返回的结果,我们所需要的所有查询信息都可以从SearchResponse中获取.
```json
"aggregations":{"sterms#creator":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":"老汉","doc_count":2},{"key":"老王","doc_count":1}]}}
```

移除了几个低频且不符合编码规范的方法:
```
allEq(Map<R, V> params)
allEq(Map<R, V> params, boolean null2IsNull)
allEq(boolean condition, Map<R, V> params, boolean null2IsNull)
```
你完全可以用eq()方法代替上述方法,可以避免代码中出现魔法值.<br />移除了几个我目前还没看到使用场景的方法:
```
having(String sqlHaving, Object... params)
having(boolean condition, String sqlHaving, Object... params)
func(Consumer<Children> consumer)
func(boolean condition, Consumer<Children> consumer)
```
新增了一些EE有但MP不支持的方法:
```
// 索引创建相关
Boolean existsIndex(String indexName);
Boolean createIndex(LambdaEsIndexWrapper<T> wrapper);
Boolean updateIndex(LambdaEsIndexWrapper<T> wrapper);
Boolean deleteIndex(String indexName);

// 高亮
highLight(高亮字段);
highLight(高亮字段,开始标签,结束标签)

// 权重
function(字段, 值, Float 权重值)

// Geo 地理位置相关
geoBoundingBox(R column, GeoPoint topLeft, GeoPoint bottomRight);
geoDistance(R column, Double distance, DistanceUnit distanceUnit, GeoPoint centralGeoPoint);
geoPolygon(R column, List<GeoPoint> geoPoints)geoShape(R column, String indexedShapeId);
geoShape(R column, String indexedShapeId);
geoShape(R column, Geometry geometry, ShapeRelation shapeRelation);

```
**4.功能阉割**<br />在全局配置,自定义注解上EE支持的功能没有MP那么多,但是已经支持的那些功能用起来与MP一样,目前已经支持了MP中常用的功能,以及高频功能,低频的后续的迭代中也会陆续跟上MP的脚步,尽量做到全支持.

除了需要注意以上列出的这些小差异,其余地方和MP并无差异,使用者完全可以像使用MP一样使用EE
