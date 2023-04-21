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

> Note that the index type of the field that needs to be matched must be text, and a tokenizer must be specified for it. The required tokenizer must be installed in advance, otherwise the es default tokenizer will be used.

- Participle matching
- Example: match("content", "Lao Wang")--->content contains the keyword 'Lao Wang' If the granularity of the word segmentation is set relatively fine, Lao Wang may be split into "Lao" and "Wang", As long as the content contains "old" or "king", it can be searched out. For other APIs, please refer to the following code examples.

Code sample:

```java

    @Test
    public void testMatch(){
        // The input will be segmented. As long as one word in all the segmented words matches the content, the data will be queried, ignoring the sequence of the segmented words.
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"man");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
    }    

    @Test
    public void testMatchPhase() {
        // The input will be segmented, but the result needs to contain all the segmented words, and the order is the same, otherwise the result will not be queried
        // For example, the data in es is beautiful girl, if the search keyword is girl beautiful, the result will not be found
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhase(Document::getContent, "beautiful girl");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMatchAllQuery() {
        // Query all data, similar to mysql select all.
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchAllQuery();
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMatchPhrasePrefixQuery() {
        // Prefix matching query Only the last word of the query string can be used as a prefix
        // The prefix may match thousands of words, which not only consumes a lot of system resources, but also the result is not very useful, so you can provide the input parameter maxExpansions, if not written, the default is 50
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.matchPhrasePrefixQuery(Document::getCustomField, "muscle man", 10);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testMultiMatchQuery() {
        // Query data containing Bob from multiple specified fields
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.multiMatchQuery("Bob", Document::getTitle, Document::getContent, Document::getCreator, Document::getCustomField);

        // Among them, the default operator is OR, and the default minShouldMatch is 60%. These two parameters can be adjusted as needed, and our api is supported. For example:
        // Where AND means that all searched Tokens must be matched, OR means that only one Token matches. minShouldMatch 80 means only query data with a matching degree greater than 80%
        // wrapper.multiMatchQuery("Bob",Operator.AND,80,Document::getCustomField,Document::getContent);

        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents.size());
        System.out.println(documents);
    }

    @Test
    public void queryStringQuery() {
        // Query data containing the keyword man from all fields
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.queryStringQuery("man");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void prefixQuery() {
        All data starting with "beautiful" by the query creator, such as beautiful girl , beautiful flower can be found
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.prefixQuery(Document::getCreator, "beautiful");
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

```
