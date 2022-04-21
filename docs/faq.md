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

