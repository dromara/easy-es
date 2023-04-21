> If you don’t want to check some large fields in some queries, you can filter the query fields

## 1.Forward filtering (only query specified fields)
```java
    @Test
    public void testFilterField() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "Hello";
        wrapper.eq(Document::getTitle, title);
        // only query title field
        wrapper.select(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
    }
```
## 2.Reverse filtering (do not query the specified field)
```java
    @Test
    public void testNotFilterField() {
        // Do not query the specified field (recommended)
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "Hello";
        wrapper.eq(Document::getTitle, title);
        // don't select title field
        wrapper.notSelect(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);

        // Another way to achieve
        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.select(Document.class, d -> !Objects.equals(d.getColumn(), "title"));
        Document document1 = documentMapper.selectOne(wrapper);
        System.out.println(document1);
    }
```
> **Tips:**
> You can only choose one of forward filtering and reverse filtering. If you use both filtering rules at the same time, the conflicting field will lose the filtering effect.

