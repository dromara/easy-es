```java
in(R column, Collection<?> value)
in(boolean condition, R column, Collection<?> value)
```

- 字段 in (value.get(0), value.get(1), ...)
- 例: in("age",{1,2,3})--->age in (1,2,3)
```java
in(R column, Object... values)
in(boolean condition, R column, Object... values)
```

- 字段 in (v0, v1, ...)
- 例: in("age", 1, 2, 3)--->age in (1,2,3)
