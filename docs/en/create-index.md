> Manually create indexes through API, we provide two ways
-Method 1: One-click creation based on entity classes and custom annotations (recommended), applicable to 99.9% of scenarios
````java
/**
 * Entity class information
**/
@Data
@TableName(shardsNum = 3, replicasNum = 2, keepGlobalPrefix = true)
public class Document {
    /**
     * The unique id in es, if you want to customize the id provided by the id in es, such as the id in MySQL, please specify the type in the annotation as customize or specify it directly in the global configuration file, so the id supports any data type)
     */
    @TableId(type = IdType.CUSTOMIZE)
    private String id;
    /**
     * Document title, if no type is specified, it will be created as keyword type by default, which can be queried accurately
     */
    private String title;
    /**
     * Document content, specifying type and storage/query tokenizer
     */
    @HighLight(mappingField = "highlightContent")
    @TableField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String content;
    // Omit other fields...
}

````

````java
 @Test
    public void testCreateIndexByEntity() {
        // Then create it directly with one click through the mapper of the entity class, which is very foolish
        documentMapper.createIndex();
    }
````

>**Tips:** For the usage of annotations in entity classes, please refer to the Annotation chapter


-Method 2: Created through API, each field that needs to be indexed needs to be processed, which is cumbersome, but has the best flexibility, supports all index creation that all ES can support, and is used for 0.01% scenarios (not recommended)

````java
    @Test
    public void testCreatIndex() {
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // For the sake of simplicity here, the index name must be consistent with the entity class name, with lowercase letters. Later chapters will teach you how to configure and use indexes more flexibly
        wrapper.indexName(Document.class.getSimpleName().toLowerCase());

        // Here, the article title is mapped to the keyword type (word segmentation is not supported), and the document content is mapped to the text type, which can be defaulted to
        // Support word segmentation query, content tokenizer can be specified, query tokenizer can also be specified, either default or only one of them can be specified, if not specified, ES default tokenizer (standard)
        wrapper.mapping(Document::getTitle, FieldType.KEYWORD)
                .mapping(Document::getContent, FieldType.TEXT,Analyzer.IK_MAX_WORD,Analyzer.IK_MAX_WORD);
        
        // If the above simple mapping cannot meet your business needs, you can customize the mapping
        // wrapper.mapping(Map);

        // Set shard and replica information, 3 shards, 2 replicas, default
        wrapper.settings(3,2);

        // If the above simple settings cannot meet your business needs, you can customize the settings
        // wrapper.settings(Settings);
        
        // Set alias information, can be default
        String aliasName = "daily";
        wrapper.createAlias(aliasName);
        
        // create index
        boolean isOk = documentMapper.createIndex(wrapper);
        Assert.assertTrue(isOk);
    }
````
> **Tips:**
> In the entity class, the id field does not need to create an index, otherwise an error will be reported.
> Due to the feature of automatic reconstruction of ES index changes, the mapping, settings, and alias information required for index creation are combined into one when this interface is designed. Although each of these configurations can be defaulted, we still recommend that you create Planning the above information in advance before indexing can avoid unnecessary troubles caused by subsequent modifications. If there are subsequent modifications, you can still use alias migration (recommended, smooth transition), or delete the original index and re-create it. Revise.