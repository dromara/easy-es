在MySQL中,我们可以通过指定字段进行group by聚合,EE同样也支持聚合:
```java
    @Test
    public void testGroupBy() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getContent,"推");
        wrapper.groupBy(Document::getCreator);
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
