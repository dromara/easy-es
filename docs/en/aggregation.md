> In MySQL, we can perform group by aggregation by specifying fields, and EE also supports aggregation:

```java
    @Test
    public void testGroupBy() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.like(Document::getContent,"world");
        wrapper.groupBy(Document::getCreator);
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
