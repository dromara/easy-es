1.当碰到有一些需求EE提供的API不支持时怎么办?<br />没关系,作者早就帮主公们想到最优的解决方案了,请查看这里:[混合查询](hybrid-query.md)

2.试用过程中,报错:java.lang.reflect.UndeclaredThrowableException
```
Caused by: [daily_document] ElasticsearchStatusException[Elasticsearch exception [type=index_not_found_exception, reason=no such index [daily_document]]]
```
如果您的错误信息和原因与上面一致,请检查索引名称是否正确配置,检查全局配置,注解配置,如果配置无误,可能是索引不存在,您可以通过es-head可视化工具查看是否已存在指定索引,若无此索引,可以通过EE提供的API快速创建.

3.依赖冲突<br />尽管EE框架足够轻量,我在研发过程中也尽量避免使用过多其它依赖,但仍难保证在极小概率下发生和宿主项目发生依赖冲突的情况,如果有依赖冲突,开发者可通过移除重复依赖或统一依赖版本号来解决,EE所有可能发生冲突的依赖如下:
```xml
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
          	<version>1.18.12</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.10.1</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
            <version>7.10.1</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.79</version>
        </dependency>
        <dependency>
             <groupId>commons-codec</groupId>
             <artifactId>commons-codec</artifactId>
             <version>1.6</version>
        </dependency>
```

4. 报错NoSuchMethod,错误信息大致如下:
> com.xpc.easyes.core.exception.EasyEsException: no such method:
	at com.xpc.easyes.core.toolkit.ExceptionUtils.eee(ExceptionUtils.java:36)
	at com.xpc.easyes.core.cache.BaseCache.lambda$setterMethod$5(BaseCache.java:94)
	at java.util.Optional.orElseThrow(Optional.java:290)

通常情况下是您实体类Model中无id字段,可复制我下面提供的示例,按需二选一,添加id字段即可,字段类型不限,但字段名称必须叫id.
```java
// 使用es自动生成的Id值
private String id;

// 如果你的id是自己指定值的,例如用MySQL中该id的值,请加注解
@TableId(type = IdType.CUSTOMIZE)
private Long id;
```
当然也有个别用户反馈说已经加了Id还是报错,不妨去掉@TableId(value="id")注解中的value="id",因为id字段在es中的命名为_id,这点差异我已在框架中做了屏蔽处理,所以用户无需再去指定value.最简单的方式就是直接复制我上面提供的代码.

还有另外一种情况也会出现NoSuchMethod,就是用户在wrapper条件中指定了高亮字段,但是未添加高亮返回值映射的新字段及注解@HighLightMappingField(value="高亮返回的新字段"),导致找不到高亮映射字段而报错. 

关于高亮的用法,如果实在不会,不妨先参考下我提供的文档:[高亮查询](highlight.md)

5.使用wrapper.eq(xx::getXX,"查询内容")查不出来数据?
eq对应的是es的TermQuery(),需要被查询字段的索引类型为keyword时才能查询,如果被查询字段的索引类型为text,那么该字段将无法被eq查询,在使用前不妨先看看自己的需求,是否需要分词匹配,如果需要分词匹配,把该字段的索引类型建立为text类型,然后使用wrapper.match(),wrapper.queryString()等方式查询;如果需要精确匹配,可将该索引字段类型建立为keyword类型,然后使用wrapper.eq()查询; 如果同一个字段,既需要用精确匹配查询,又需要被分词查询,不妨将该字段冗余,新增一个字段,值与该字段保持一致,一个索引用keyword,一个用text类型,这样就可以完美化解了,对es而言支持PB级数据,增加一个冗余字段,对性能影响微乎其微.

> **Tips** 在框架使用中,难免会因为各种原因导致异常,我们不排除框架本身有缺陷导致,但目前发布的功能中,绝大多数都是比较稳定且有测试用例覆盖及大量用户生产环境验证过的,更多的时候是用户没有按文档使用,自由发挥,导致出现一些问题,这类用户通常还比较懒,一碰到鸡毛大点问题马上来群里问,或是抱怨框架垃圾,然后我们协助排查解决最后发现是xx地方没有按文档使用,而是胡乱搞,我们也很无奈,毕竟做开源,精力和时间也比较有限,我们想把时间花在刀刃上,比如收集真正的bug,去迭代解决,而不是把时间浪费在这些无谓的地方.
所以我们还是希望用户能在使用前多读文档,遇到问题不妨先从文档下手,看看我们提供的DEMO是怎么写的? 打断点找找原因,看看源码分析分析,经历了这些步骤,如果仍然解决不了,可以再来答疑群里问,这是一个码农基本的素养,而且对提升自身技术水平有很大帮助,如果碰到问题就抛出去,久而久之,自我独立解决和分析问题的能力会越来越差,长此以往,若有一天用了某款开源产品,碰到问题恰好没人答疑,又当何去何从? 


