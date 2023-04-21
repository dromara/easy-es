```java
    // 物理分页
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize);
```
> Tips:
> - 无需集成任何插件,即可使用分页查询,本查询属于物理分页.
> - 在一些高阶语法的使用场景中,目前已知的有聚合字段的返回,我们分页器尚不能支持,需要您自己封装分页,其它场景基本都能完美支持,用起来无比简单.
> - 注意PageInfo是由本框架提供的,如果你项目中已经有目前最受欢迎的开源分页插件PageHelper,请在引入包的时候注意别引入错误了,EE采用和PageHelper一样的返回字段,您无需担心字段名称不统一带来的额外工作量.

> 使用示例:

```java
    @Test
    public void testPageQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getTitle, "老汉");
        PageInfo<Document> documentPageInfo = documentMapper.pageQuery(wrapper,1,10);
        System.out.println(documentPageInfo);
    }
```