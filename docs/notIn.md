```java
notIn(R column, Collection<?> value)
notIn(boolean condition, R column, Collection<?> value)
```

- 字段 not in (value.get(0), value.get(1), ...)
- 例: notIn("age",{1,2,3})--->age not in (1,2,3)
```java
notIn(R column, Object... values)
notIn(boolean condition, R column, Object... values)
```

- 字段 not in (v0, v1, ...)
- 例: notIn("age", 1, 2, 3)--->age not in (1,2,3)
