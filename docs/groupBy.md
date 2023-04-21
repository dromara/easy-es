```java
groupBy(R... columns)
groupBy(boolean condition, R... columns)
```

- 分组：GROUP BY 字段, ...
- 例: groupBy(Document::getId,Document::getTitle)--->group by id,title
