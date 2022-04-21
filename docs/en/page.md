```java
	// The return type is not specified, and the paging parameter is not specified (the current page is 1 by default, and the total number of queries is 10), suitable for queries with high-level grammar
    PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper) throws IOException;

	// No return type specified, paging parameters specified, suitable for queries with high-level syntax
    PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) throws IOException;

	// 指定返回类型,但未指定分页参数(默认按当前页为1,总查询条数10条)
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper);

	// Specify the return type and paging parameters
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize);
```
> **Tips:**
> - You can use paging query without integrating any plug-ins. This query belongs to physical paging.
> - In some high-level grammar usage scenarios, due to the return of highlighting, aggregation and other fields, it is recommended to use the native SearchHit to receive the result set. Otherwise, because the entity itself does not contain fields such as highlighting and aggregation, this type of Field is missing.
> - Note that PageInfo is provided by this framework. If you already have the most popular open source paging plugin [PageHelper](https://github.com/pagehelper/Mybatis-PageHelper) in your project, please be careful not to introduce errors when importing the package. EE uses the same return fields as PageHelper, so you don't need to worry about the field names. The extra workload caused by inconsistency.

