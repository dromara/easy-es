```java
match(R column, Object val)
match(boolean condition, R column, Object val)
```

- 分词匹配
- 例: match("content", "老王")--->content 包含关键词 '老王' 如果分词粒度设置的比较细,老王可能会被拆分成"老"和"王",只要content中包含"老"或"王",均可以被搜出来
