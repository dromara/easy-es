```java
orderBy(boolean condition, boolean isAsc, R... columns)
```
● Sorting: ORDER BY field, ...<br />● Example: orderBy(true, true, Document::getId,Document::getTitle)--->order by id ASC,title ASC
