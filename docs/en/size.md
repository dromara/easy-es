```java
size(Integer size)
```
● How many pieces of data are returned at most, which is equivalent to n in limit (m,n) or n in limit n in MySQL<br />● Example: size(10)--->Only return 10 pieces of data at most
> **Tips: **If the size parameter is not specified, its default value is 10000
> If you do a single query and don't want too much data with low scores, you need to manually specify the size to limit it.

