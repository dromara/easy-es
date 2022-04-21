> The word segmentation query is unique to Es. It is a query that is not supported in MySQL, that is, you can match according to keywords. There is not much introduction about word segmentation query. If you don't know it, please Google search to understand the concept, here only the usage is introduced.

```java
    @Test
    public void testMatch(){
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "w";
        wrapper.match(Document::getContent,keyword);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
