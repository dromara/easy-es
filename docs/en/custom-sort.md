>  **Background:** A certain degree of interface support is provided for some unconventional and low-frequency sorting. Compared with the previously provided solution "pure hybrid query", there are certain optimizations, and the native query interface can no longer be used.
> Because ES provides a lot of sorting methods, these sorting methods are very complex and flexible, and there is no way to integrate them in a short period of time, and some have not yet figured out how to integrate them. It will be more convenient for users to use, and such sorting Compared with the several sorting methods provided, it is more low-frequency, so it is the most flexible to entrust the sorting builder to the user, which is a better solution in the transition period. This API can 100% support all the query functions provided by ES. We will continue to iterate and absorb user feedback. In the near future, we will also provide out-of-the-box API support for such super-complex sorting, so stay tuned.

```java
// api (0.9.7+ version support)
wrapper.sort(boolean condition, SortBuilder<?> sortBuilder)
```
Example of use:
```java
    @Test
    public void testSort(){
        // To test complex sorting, there are many subclasses of SortBuilder, only one of which is demonstrated here. For example, some users propose to obtain data randomly
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"hello");
        Script script = new Script("Math.random()");
        ScriptSortBuilder scriptSortBuilder = new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.NUMBER);
        wrapper.sort(scriptSortBuilder);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
The SortBuilder class has many subclasses and is very flexible, so there are enough sorting scenarios that can be supported and covered. For other types of queries, if you encounter them during use, you can refer to the above examples to write.
