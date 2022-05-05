> 为了方便用户去重,我们针对字段的去重提供了极为友好的方式(v0.9.12+支持),解决用户根据字段进行去重及分页要写大量代码来实现的烦恼,使用ee去重仅需1行即可搞定!

API:
```java
    // 去重,入参为去重列
    wrapper.distinct(R column);
```

下面我用一段代码来演示根据指定字段去重:

```java
    @Test
    public void testDistinct() {
        // 查询所有标题为老汉的文档,根据创建者去重,并分页返回
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .distinct(Document::getCreator);
        PageInfo<Document> pageInfo = documentMapper.pageQuery(wrapper, 1, 10);
        System.out.println(pageInfo);
    }
```

就是这么easy! 傻瓜都会,你学废了吗? 隔壁老王家小孩问我有什么了不起,支持多字段去重吗? 答案是支持的,但没这么方便.因为多字段去重无法通过折叠去实现,数据会被置入桶中返回,桶中数据的解析,需要哪些字段,排序规则,覆盖规则是怎样的过于灵活,我们无法通过框架来帮用户屏蔽这些,因此,我们对多字段的去重仅支持到了查询条件的封装,数据解析部分需用户自行完成,敬请谅解,我们已经尽力了,这些功能连springdata都弃疗了.好在多字段去重的场景并不是太多,用户如用到可移步至[聚合模块](aggregation.md)查看多字段groupBy相关文档.
