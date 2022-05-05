```java
match(boolean condition, R column, Object val);
notMatch(boolean condition, R column, Object val, Float boost);
matchPhase(boolean condition, R column, Object val, Float boost);
matchAllQuery();
matchPhrasePrefixQuery(boolean condition, R column, Object val, int maxExpansions, Float boost);
multiMatchQuery(boolean condition, Object val, Operator operator, int minimumShouldMatch, Float boost, R... columns);
queryStringQuery(boolean condition, String queryString, Float boost);
prefixQuery(boolean condition, R column, String prefix, Float boost);
```

> 注意,涉及需要分词匹配的字段索引类型必须为text,并为其指定分词器,所需分词器需提前安装,否则将使用es默认分词器.

- 分词匹配
- 例: match("content", "老王")--->content 包含关键词 '老王' 如果分词粒度设置的比较细,老王可能会被拆分成"老"和"王",只要content中包含"老"或"王",均可以被搜出来,其它api可参考下面代码示例.

代码示例:

```java

    @Test
    public void testMatch(){
        // 会对输入做分词,只要所有分词中有一个词在内容中有匹配就会查询出该数据,无视分词顺序
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"技术");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
    }    

    @Test
    public void testMatchPhase() {
        // 会对输入做分词，但是需要结果中也包含所有的分词，而且顺序要求一样,否则就无法查询出结果
        // 例如es中数据是 技术过硬,如果搜索关键词为过硬技术就无法查询出结果
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhase(Document::getContent, "技术");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMatchAllQuery() {
        // 查询所有数据,类似mysql select all.
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchAllQuery();
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMatchPhrasePrefixQuery() {
        // 前缀匹配查询 查询字符串的最后一个词才能当作前缀使用
        // 前缀 可能会匹配成千上万的词,这不仅会消耗很多系统资源,而且结果的用处也不大,所以可以提供入参maxExpansions,若不写则默认为50
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrasePrefixQuery(Document::getCustomField, "乌拉巴拉", 10);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMultiMatchQuery() {
        // 从多个指定字段中查询包含老王的数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.multiMatchQuery("老王", Document::getTitle, Document::getContent, Document::getCreator, Document::getCustomField);

        // 其中,默认的Operator为OR,默认的minShouldMatch为60% 这两个参数都可以按需调整,我们api是支持的 例如:
        // 其中AND意味着所有搜索的Token都必须被匹配,OR表示只要有一个Token匹配即可. minShouldMatch 80 表示只查询匹配度大于80%的数据
        // wrapper.multiMatchQuery("老王",Operator.AND,80,Document::getCustomField,Document::getContent);

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
        System.out.println(documents);
    }

    @Test
    public void queryStringQuery() {
        // 从所有字段中查询包含关键词老汉的数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.queryStringQuery("老汉");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void prefixQuery() {
        // 查询创建者以"隔壁"打头的所有数据  比如隔壁老王 隔壁老汉 都能被查出来
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.prefixQuery(Document::getCreator, "隔壁");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

```
