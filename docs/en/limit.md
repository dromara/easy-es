```java
limit(Integer n);

limit(Integer m, Integer n);
```
● limit n is the maximum number of pieces of data returned, which is equivalent to n in limit n in MySQL, and the usage is the same.<br />● limit m,n skip m pieces of data and return n pieces of data at most, which is equivalent to limit m,n or offset m limit n in MySQL<br />● Example: limit(10)--->Only return up to 10 pieces of data<br />● Example: limit(2,5)--->Skip the first 2 pieces of data, start the query from the 3rd piece, and query 5 pieces of data in total

> **Tips:** If the n parameter is not specified, its default value is 10000
> If you do a single query and don't want too many low-scoring data, you need to manually specify n to limit it.
> In addition, the function of this parameter is consistent with the size and from in Es. It is only introduced for compatibility with MySQL syntax. Users can choose one of the two according to their own habits. When both are used, only one will take effect, and the later specified will override the first specified. of.

