> In MySQL, we can perform group by aggregation by specifying fields, and EE also supports aggregation:

```java
    @Test
    public void testGroupBy() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.like(Document::getContent,"world");
        wrapper.groupBy(Document::getCreator);
        // if multiple fields
        // wrapper.groupBy(Document::getCreator,Document::getModifier);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```
> **Tips:**
> The aggregation result of Es is placed in a separate object, so we need to use a semi-native query method to return SearchResponse to get the aggregation result

Other aggregations:
```java
// Find the minimum
wrapper.min();
// Find the maximum
wrapper.max();
// average
wrapper.avg();
wrapper.sum();
```

If you need to groupBy first, and then calculate the maximum value, mean, etc. according to the data in the bucket after aggregation by groupBy, it is also supported, and it will be chained according to the order you specified in the wrapper (pipeline aggregation).

Example:

```java
     @Test
     public void testAgg() {
         // Aggregate according to the creator, and aggregate again according to the number of likes in the bucket after the aggregation
         // Note: The specified multiple aggregation parameters are chain aggregation, which is the result of the aggregation of the first aggregation parameter, and then aggregated according to the second parameter, corresponding to the Pipeline aggregation
         LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
         wrapper.eq(Document::getTitle, "Old man")
                 .groupBy(Document::getCreator)
                 .max(Document::getStarNum);
         SearchResponse response = documentMapper.search(wrapper);
         System.out.println(response);
     }
```