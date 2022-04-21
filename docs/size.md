```java
size(Integer size)
```

- 最多返回多少条数据,相当于MySQL中limit (m,n)中的n 或limit n 中的n
- 例: size(10)--->最多只返回10条数据
> **Tips:**size参数若不指定,则其默认值是10000
> 如果你单次查询,不想要太多得分较低的数据,需要手动指定size去做限制.

