```java
	// 半原生查询
    SearchResponse search(LambdaEsQueryWrapper<T> wrapper) throws IOException;
	
	// 标准原生查询 可指定 RequestOptions
    SearchResponse search(SearchRequest searchRequest, RequestOptions requestOptions) throws IOException;
```
> Tips:
> - 在一些高阶语法中,比如指定高亮字段,如果我们返回类型是实体对象本身,但实体中通常又没有高亮字段,导致高亮字段无法接收,此时可以用RestClietn原生的返回对象SearchResponse.
> - 尽管EE覆盖了我们使用ES的绝大多场景,但仍可能存在没有覆盖到的场景,此时您仍可以通过RestClient提供的原生语法进行查询,调用标准原生查询方法即可,入参和返回均为RestClient原生

