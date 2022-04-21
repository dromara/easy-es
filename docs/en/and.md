```java
and(Consumer<Param> consumer)
and(boolean condition, Consumer<Param> consumer)
```
● AND nesting<br />● Example: and(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->and (title ='Hello' and creator != 'Guy' )
