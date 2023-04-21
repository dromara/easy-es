> The weight query is also a query that Es has and MySQL does not have, and the syntax is as follows

```java
function(field, value, Float weightsValue)
```
```java
    @Test
    public void testWeight() throws IOException {
      	// 测试权重
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "Hello";
        float contentBoost = 5.0f;
        wrapper.match(Document::getContent,keyword,contentBoost);
        String creator = "Guy";
        float creatorBoost = 2.0f;
        wrapper.eq(Document::getCreator,creator,creatorBoost);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```
> Tips:
> If you need a score, you can return it through SearchResponse. If you don't need a score, you only need to return it according to the ranking with the highest score, and you can directly use List<T> to receive it.

