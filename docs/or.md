```java
or()
or(boolean condition)
```

- 拼接 OR**注意事项:**主动调用or表示紧接着下一个**方法**不是用and连接!(不调用or则默认为使用and连接)
- 例: eq("Document::getId",1).or().eq(Document::getTitle,"Hello")--->id = 1 or title ='Hello'
```java
or(Consumer<Param> consumer)
or(boolean condition, Consumer<Param> consumer)
```

- OR 嵌套
- 例: or(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->or (title ='Hello' and status != 'Guy' )
