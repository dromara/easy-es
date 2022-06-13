> 通过API手动创建索引,我们提供了两种方式
-方式一:根据实体类及自定义注解一键创建(推荐),99.9%场景适用
```java
/**
 * 实体类信息
**/
@Data
@TableName(shardsNum = 3, replicasNum = 2, keepGlobalPrefix = true)
public class Document {
    /**
     * es中的唯一id,如果你想自定义es中的id为你提供的id,比如MySQL中的id,请将注解中的type指定为customize或直接在全局配置文件中指定,如此id便支持任意数据类型)
     */
    @TableId(type = IdType.CUSTOMIZE)
    private String id;
    /**
     * 文档标题,不指定类型默认被创建为keyword类型,可进行精确查询
     */
    private String title;
    /**
     * 文档内容,指定了类型及存储/查询分词器
     */
    @HighLight(mappingField = "highlightContent")
    @TableField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String content;
    // 省略其它字段...
}

```

```java
 @Test
    public void testCreateIndexByEntity() {
        // 然后通过该实体类的mapper直接一键创建,非常傻瓜级
        documentMapper.createIndex();
    }
```

>**Tips:** 实体类中的注解用法可参考注解章节


-方式二:通过api创建,每个需要被索引的字段都需要处理,比较繁琐,但灵活性最好,支持所有es能支持的所有索引创建,供0.01%场景使用(不推荐)

```java
    @Test
    public void testCreatIndex() {
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 此处简单起见 索引名称须保持和实体类名称一致,字母小写 后面章节会教大家更如何灵活配置和使用索引
        wrapper.indexName(Document.class.getSimpleName().toLowerCase());

        // 此处将文章标题映射为keyword类型(不支持分词),文档内容映射为text类型,可缺省
        // 支持分词查询,内容分词器可指定,查询分词器也可指定,,均可缺省或只指定其中之一,不指定则为ES默认分词器(standard)
        wrapper.mapping(Document::getTitle, FieldType.KEYWORD)
                .mapping(Document::getContent, FieldType.TEXT,Analyzer.IK_MAX_WORD,Analyzer.IK_MAX_WORD);
        
        // 如果上述简单的mapping不能满足你业务需求,可自定义mapping
        // wrapper.mapping(Map);

        // 设置分片及副本信息,3个shards,2个replicas,可缺省
        wrapper.settings(3,2);

        // 如果上述简单的settings不能满足你业务需求,可自定义settings
        // wrapper.settings(Settings);
        
        // 设置别名信息,可缺省
        String aliasName = "daily";
        wrapper.createAlias(aliasName);
        
        // 创建索引
        boolean isOk = documentMapper.createIndex(wrapper);
        Assert.assertTrue(isOk);
    }
```
> **Tips:**
> 实体类中,id字段不需要创建索引,否则会报错.
> 由于ES索引改动自动重建的特性,因此本接口设计时将创建索引所需的mapping,settings,alias信息三合一了,尽管其中每一项配置都可缺省,但我们仍建议您在创建索引前提前规划好以上信息,可以规避后续修改带来的不必要麻烦,若后续确有修改,您仍可以通过别名迁移的方式(推荐,可平滑过渡),或删除原索引重新创建的方式进行修改.

