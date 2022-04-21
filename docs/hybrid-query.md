# 何为混合查询? 
简单理解,就是一半采用EE的语法,一半采用RestHighLevelClient的语法,类似"油电混动",相信你会爱上这种"油电混动"模式,因为它结合了两种模式的优点!
# 为什么要有混合查询? 
因为EE目前还没有做到对RestHighLevelClient的功能100%覆盖,目前开源初期,仅覆盖了RestHighLevelClient约90%左右的功能,99%的核心高频使用功能,如此就不可避免的会出现个别场景下,EE不能满足某个特殊需求,此时对EE框架进行二次开发或直接将该需求提给EE作者,在时间上都无法满足开发者需求,有些需求可能产品经理要的比较紧,那么此时,您就可以通过混合查询来解决窘境.
# 如何使用混合查询?
在我没提供此篇文档时,尽管我提供了混合查询的API和简单介绍,但很多人还不知道有此功能,更不知道该如何使用,所以这里我以一个具体的案例,给大家演示如何使用混合查询,供大家参考,主公们别担心篇幅多,其实非常非常简单,只是我教程写的细.

> **背景:** 用户"向阳"微信向我反馈,说目前EE尚不支持查询按照距给定点的位置由近及远排序.
> 实际开发中,此场景可以被应用到打车时"乘客下单,要求优先派单给周围3公里内离我最近的司机",然后该乘客是个美女,担心自身安全问题,又多加了几个要求,比如司机必须是女性,驾龄大于3年,商务型车子等...


以上面打车的场景为例,我们来看下用EE怎么查询?上面查询可以分为两部分

- EE支持的常规查询:如周围3公里内,司机性别为女,查询驾龄>=3年...
- EE不支持的非常规查询:按照复杂的排序规则排序(目前EE仅支持常规字段的升序/降序排序)

对于支持的部分,我们可以直接调用EE,由EE先构建一个SearchSourceBuilder出来
```java
// 假设该乘客所在位置经纬度为 31.256224D, 121.462311D
LambdaEsQueryWrapper<Driver> wrapper = new LambdaEsQueryWrapper<>();
wrapper.geoDistance(Driver::getLocation, 3.0, DistanceUnit.KILOMETERS, new GeoPoint(31.256224D, 121.462311D));
       .eq(Driver::getGender,"女"")
       .ge(Driver::getDriverAge,3)
       .eq(Driver::getCarModel,"商务车");
SearchSourceBuilder searchSourceBuilder = driverMapper.getSearchSourceBuilder(wrapper);
```
对于不支持的语句,可以继续用RestHighLevelClient的语法进行封装,封装好了,直接调用EE提供的原生查询接口,就可以完成整个查询.
```java
SearchRequest searchRequest = new SearchRequest("索引名");
// 此处的searchSourceBuilder由上面EE构建而来,我们继续对其追加排序参数
searchSourceBuilder.sort(
        new GeoDistanceSortBuilder("location", 31.256224D, 121.462311D)
                 .order(SortOrder.DESC)
                 .unit(DistanceUnit.KILOMETERS)
                 .geoDistance(GeoDistance.ARC)
);
searchRequest.source(searchSourceBuilder);
SearchResponse searchResponse = driverMapper.search(searchRequest, RequestOptions.DEFAULT);
```

 如此您便可以既享受到了EE帮您生成好的基本查询,又可完成EE暂未支持的功能,只需要不太多的代码(相比直接RestHighLevelClient,仍能节省大量代码)就可以达成您的目标,和当下纯电动汽车尚未完全发展成熟下的一种折中方案---油电混动有着异曲同工之妙.

当然,如果您不习惯使用这种模式,您仍可以直接使用原生查询,所以您大可以无忧无虑的使用EE,我们已经为您想好了各种兜底的方案和退路,无忧售后!如果您也认可这种模式,不妨给作者点个赞吧,为了让EE的用户爽,作者那糟老头子可谓是煞费苦心!

# 结语

因为ES官方提供的RestHighLevel支持的功能实在是过于繁多,尽管我目前仍在马不停蹄的集成各种新的功能,以及修复用户反馈的问题,优化既有代码性能,但仍不可避免地会出现有些许功能不能满足您当前需求,请各位主公们见谅,EE才诞生三个月,不可能做到十全十美,请给我们一点时间,这些所谓的不足,都会被解决,就像新能源车在未来终会逐步取代燃油车,那些所谓的问题,在未来都不是问题,乌拉!

