```java
set(String column, Object val)
set(boolean condition, String column, Object val)
```

- SQL SET 字段
- 例: set("name", "老李头")
- 例: set("name", "")--->数据库字段值变为**空字符串**
- 例: set("name", null)--->数据库字段值变为null
