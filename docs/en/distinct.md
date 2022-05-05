> In order to facilitate users to deduplicate, we provide a very friendly method for field deduplication (v0.9.12+ support), which solves the trouble that users need to write a lot of code for deduplication and paging according to fields. Using ee to deduplicate only 1 line to do it!

API:
```java
    // De-duplication, input parameters are de-duplication
    wrapper.distinct(R column);
```

Below I use a piece of code to demonstrate deduplication based on specified fields:

```java
     @Test
     public void testDistinct() {
         // Query all documents with the title of the old man, deduplicate according to the creator, and return in pagination
         LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
         wrapper.eq(Document::getTitle, "boy")
                 .distinct(Document::getCreator);
         PageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 10);
         System.out.println(pageInfo);
     }
```

It’s so easy! Even fools can do it, have you learned it? Does it support multi-field deduplication? The answer is yes, but it’s not so convenient. Because multi-field deduplication cannot be achieved by folding, the data will be placed in the bucket and returned. What fields are required for data parsing, sorting rules, and coverage rules are too flexible. We cannot help users shield these through the framework. Therefore, our deduplication of multiple fields only supports the encapsulation of query conditions and the data parsing part. It needs to be done by the user, please understand, we have done our best, these functions have been abandoned even in springdata. Fortunately, there are not too many scenarios for multi-field deduplication. If you use it, you can move to the aggregation module to view multi-field groupBy related documents.