```java
orderByAsc(R... columns)
orderByAsc(boolean condition, R... columns)
```
● Sort: ORDER BY field, ... ASC<br />● Example: orderByAsc(Document::getId,Document::getTitle)--->order by id ASC,title ASC
