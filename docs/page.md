```java
	// 未指定返回类型,未指定分页参数(默认按当前页为1,总查询条数10条) 适合有高阶语法时的查询
    PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper) throws IOException;

	// 未指定返回类型,指定分页参数 适合有高阶语法时的查询
    PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) throws IOException;

	// 指定返回类型,但未指定分页参数(默认按当前页为1,总查询条数10条)
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper);

	// 指定返回类型及分页参数
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize);
```
> Tips:
> - 无需集成任何插件,即可使用分页查询,本查询属于物理分页.
> - 在一些高阶语法的使用场景中,由于有高亮,聚合等字段的返回,建议使用原生的SearchHit进行结果集的接收,否则由于实体中本身不含高亮及聚合等字段,会导致这类字段丢失.
> - 注意PageInfo是由本框架提供的,如果你项目中已经有目前最受欢迎的开源分页插件PageHelper,请在引入包的时候注意别引入错误了,EE采用和PageHelper一样的返回字段,您无需担心字段名称不统一带来的额外工作量.

