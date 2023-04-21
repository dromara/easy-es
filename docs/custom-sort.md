> 背景:针对一些非常规低频使用的排序予以一定程度的接口支持,相较于之前已经提供的解决方案"纯混合查询",有一定的优化,可以不再使用原生查询接口.
> 由于ES提供了非常多的排序方式,这些排序方式十分复杂和灵活,,短时间内没有办法都集成进来,有些也尚未想好以怎样的方式集成进来用户使用起来会更方便,加之此类排序相较已提供的几种排序方式更为低频,故而将排序建造者委托给用户使用最为灵活,是目前过渡期比较好的解决方案,此API可以100%支持ES提供的所有查询功能. 随着不断迭代和吸纳用户反馈,在不久的将来,对此类超复杂排序我们也会提供开箱即用的API支持,敬请期待.


```java
// api (0.9.7+ 版本支持)
wrapper.sort(boolean condition, SortBuilder<?> sortBuilder)
```

使用示例:
```java
    @Test
    public void testSort(){
        // 测试复杂排序,SortBuilder的子类非常多,这里仅演示一种, 比如有用户提出需要随机获取数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"技术");
        Script script = new Script("Math.random()");
        ScriptSortBuilder scriptSortBuilder = new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.NUMBER);
        wrapper.sort(scriptSortBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
SortBuilder类的子类非常多,也非常灵活,所以能支撑和覆盖的排序场景也足够多,其它各种类型的查询,如果您在使用过程中有碰到,可以参考上面的例子去写.
