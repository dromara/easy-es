在MySQL中,我们可以通过指定字段进行group by聚合,EE同样也支持聚合:
```java
    @Test
    public void testGroupBy() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getContent,"推");
        wrapper.groupBy(Document::getCreator);
        // 支持多字段聚合
        // wrapper.groupBy(Document::getCreator,Document::getCreator);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```
> **Tips:**
> 尽管语法与MP一致,但实际上,ES的聚合结果是放在单独的对象中的,格式如下所示,因此我们高阶语法均需要用SearchResponse来接收返回结果,这点需要区别于MP和MySQL.

```json
"aggregations":{"sterms#creator":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":"老汉","doc_count":2},{"key":"老王","doc_count":1}]}}
```
其它聚合:
```json
// 求最小值
wrapper.min();
// 求最大值
wrapper.max();
// 求平均值
wrapper.avg();
// 求和
wrapper.sum();
```
如果需要先groupBy,再根据grouBy聚合后桶中的数据进行求最值,均值之类的,也是支持的,会按照您在wrapper中指定的顺序,管道聚合(pipeline aggregation).

示例:

```java
    @Test
    public void testAgg() {
        // 根据创建者聚合,聚合完在该桶中再次根据点赞数聚合
        // 注意:指定的多个聚合参数为管道聚合,就是第一个聚合参数聚合之后的结果,再根据第二个参数聚合,对应Pipeline聚合
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(Document::getTitle, "老汉")
                .groupBy(Document::getCreator)
                .max(Document::getStarNum);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```

> 在0.9.14+版本,我们进一步强化了聚合api,提供了可以配置是否开启管道聚合的参数,默认为开启,如果你想让多个字段聚合的结果出现在各自的桶中,那么你可以指定eanblePipeline参数为false即可.

```java
    @Test
    public void testAggNotPipeline() {
        // 对于下面两个字段,如果不想以pipeline管道聚合,各自聚合的结果在各自的桶中展示的话,我们也提供了支持
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        // 指定启用管道聚合为false
        wrapper.groupBy(false, Document::getCreator, Document::getTitle);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```