如果您在某些查询中,不想查一些大字段,您可以过滤查询的字段

1. 正向过滤(只查询指定字段)
```java
    @Test
    public void testFilterField() {
        // 测试只查询指定字段
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老汉";
        wrapper.eq(Document::getTitle, title);
        wrapper.select(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);
    }
```

2. 反向过滤(不查询指定字段)
```java
    @Test
    public void testNotFilterField() {
        // 测试不查询指定字段 (推荐)
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String title = "老汉";
        wrapper.eq(Document::getTitle, title);
        wrapper.notSelect(Document::getTitle);
        Document document = documentMapper.selectOne(wrapper);
        System.out.println(document);

        // 另外一种与mp一致语法的Lambda写法
        LambdaEsQueryWrapper<Document> wrapper1 = new LambdaEsQueryWrapper<>();
        wrapper1.select(Document.class, d -> !Objects.equals(d.getColumn(), "title"));
        Document document1 = documentMapper.selectOne(wrapper);
        System.out.println(document1);
    }
```
> **Tips:**
> 正向过滤和反向过滤你只能选择其中一种,如果同时使用两种过滤规则,会导致冲突字段失去过滤效果.

