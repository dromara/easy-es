```java
notIn(R column, Collection<?> value)
notIn(boolean condition, R column, Collection<?> value)
```
● Field not in (value.get(0), value.get(1), ...)<br />● Example: notIn("age",{1,2,3})--->age not in (1,2,3)

```java
notIn(R column, Object... values)
notIn(boolean condition, R column, Object... values)
```
● Field not in (v0, v1, ...)<br />● Example: notIn("age", 1, 2, 3)--->age not in (1,2,3)
