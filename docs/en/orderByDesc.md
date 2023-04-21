```java
orderByDesc(R... columns)
orderByDesc(boolean condition, R... columns)
```
● Sorting: ORDER BY field, ... DESC<br />● Example: orderByDesc(Document::getId,Document::getTitle)--->order by id DESC,title DESC
