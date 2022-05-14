```java
    @Test
    public void testCreatIndex() {
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // For the sake of simplicity here, the index name must be consistent with the entity class name, with lowercase letters. Later chapters will teach you how to configure and use the index more flexibly
        wrapper.indexName(Document.class.getSimpleName().toLowerCase());

        // Here, the article title is mapped to the keyword type (word segmentation is not supported), and the document content is mapped to the text type (word segmentation query is supported), which can be defaulted
        wrapper.mapping(Document::getTitle, FieldType.KEYWORD)
                .mapping(Document::getContent, FieldType.TEXT);

        // Version 0.9.8+ supports passing in field name String directly
        wrapper.mapping("wu-la", FieldType.TEX);

        // If the above simple mapping cannot meet your business needs, you can customize the mapping
        // wrapper.mapping(Map);

        // Set shard and replica information, here 3 shards and 2 replicas are set, which can be defaulted
        wrapper.settings(3,2);

        // If the above simple settings cannot meet your business needs, you can customize the settings
        // wrapper.settings(Settings);
        
        // Set alias information, which can be defaulted
        String aliasName = "dev";
        wrapper.createAlias(aliasName);
        
        // execute create index
        boolean isOk = documentMapper.createIndex(wrapper);
        Assert.assertTrue(isOk);
    }
```
> **Tips:**
> Due to the feature of automatic reconstruction of ES index changes, the mapping, settings, and alias information required to create the index are three-in-one during the design of this interface. Although each configuration can be defaulted, we still recommend that you create the index plan the above information in advance to avoid unnecessary trouble caused by subsequent modifications. If there are subsequent modifications, you can still modify it by alias migration (recommended, smooth transition), or delete the original index and recreate it. .
> 

