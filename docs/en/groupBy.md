```java
groupBy(R... columns)
groupBy(boolean condition, R... columns)
```
● Grouping: GROUP BY field, ...<br />● Example: groupBy(Document::getId,Document::getTitle)--->group by id,title
