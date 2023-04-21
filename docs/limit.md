```java
limit(Integer n);

limit(Integer m, Integer n);
```

- limit n 最多返回多少条数据,相当于MySQL中limit n 中的n,用法一致.
- limit m,n 跳过m条数据,最多返回n条数据,相当于MySQL中的limit m,n 或 offset m  limit n
- 例: limit(10)--->最多只返回10条数据
- 例: limit(2,5)--->跳过前2条数据,从第3条开始查询,总共查询5条数据

> **Tips:** n参数若不指定,则其默认值是10000 如果你单次查询,不想要太多得分较低的数据,需要手动指定n去做限制.

> 另外此参数作用与Es中的size,from一致,只是为了兼容MySQL语法而引入,使用者可以根据自身习惯二选一,当两种都用时,只有一种会生效,后指定的会覆盖先指定的.

