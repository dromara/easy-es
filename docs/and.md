```java
and(Consumer<Param> consumer)
and(boolean condition, Consumer<Param> consumer)
```

- AND 嵌套
- 例: and(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->and (title ='Hello' and creator != 'Guy' )
