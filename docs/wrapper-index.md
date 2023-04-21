```java
index(String indexName)
eq(boolean condition, String indexName)
```
> 可通过wrapper.index(String indexName)指定本次查询作用于哪个索引,如果本次查询要从多个索引上查询,那么索引名称可以用逗号隔开,例如wrapper.eq("index1","indexes").
> wrapper中指定的索引名称优先级最高,如果不指定则取实体类中配置的索引名称,如果实体类也未配置,则取实体名称小写作为当前查询的索引名
> 针对insert/delete/update等接口中无wrapper的情况,如果你需要指定索引名,可直接在对应接口的入参中添加索引名称,可参考下面示例:

```java
    Document document = new Document();
    // 省略为document赋值的代码
    String indexName = "laohan";
    insert(document,indexName);
```