In many scenarios, due to changes in the entity model or requirements, we need to update the index. For example, I added an author field creator to the document entity model. At this time, I want to update the index so that it can be based on the author keywords. search
```java
    @Test
    public void testUpdateIndex(){
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        String indexName = Document.class.getSimpleName().toLowerCase();
        wrapper.indexName(indexName);
        wrapper.mapping(Document::getCreator,FieldType.KEYWORD);
        boolean isOk = documentMapper.updateIndex(wrapper);
        Assert.assertTrue(isOk);
    }
```
> **Tips:**
> - If your production environment requires a smooth transition, then we do not recommend updating the index in this way, because updating the mapping will cause Es to rebuild the index. In such cases, it is recommended to migrate through the alias method.
> - indexName cannot be empty, you must specify which index to update
> - This interface only supports mapping to update the index. If you need to update information such as shards, data sets, index names, etc., it is recommended to call the delete index interface to delete the original index, and then call the index creation interface to re-create the index (low frequency operation, later versions can be based on the user Feedback to decide whether to join the support update)

