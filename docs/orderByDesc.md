```java
orderByDesc(R... columns)
orderByDesc(boolean condition, R... columns)
```

- 排序：ORDER BY 字段, ... DESC
- 例: orderByDesc(Document::getId,Document::getTitle)--->order by id DESC,title DESC
