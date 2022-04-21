```java
    @Test
    public void testCreatIndex() {
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 此处简单起见 索引名称须保持和实体类名称一致,字母小写 后面章节会教大家更如何灵活配置和使用索引
        wrapper.indexName(Document.class.getSimpleName().toLowerCase());

        // 此处将文章标题映射为keyword类型(不支持分词),文档内容映射为text类型,可缺省
        // 支持分词查询,内容分词器可指定,查询分词器也可指定,,均可缺省或只指定其中之一,不指定则为ES默认分词器(standard)
        wrapper.mapping(Document::getTitle, FieldType.KEYWORD)
                .mapping(Document::getContent, FieldType.TEXT,Analyzer.IK_MAX_WORD,Analyzer.IK_MAX_WORD);
        // 0.9.8 + 版本支持直接传入字段名称字符串
        wrapper.mapping("wu-la", FieldType.TEX);

        // 设置分片及副本信息,3个shards,2个replicas,可缺省
        wrapper.settings(3,2);
        
        // 设置别名信息,可缺省
        String aliasName = "daily";
        wrapper.createAlias(aliasName);
        
        // 创建索引
        boolean isOk = documentMapper.createIndex(wrapper);
        Assert.assertTrue(isOk);
    }
```
> **Tips:**
> 由于ES索引改动自动重建的特性,因此本接口设计时将创建索引所需的mapping,settings,alias信息三合一了,尽管其中每一项配置都可缺省,但我们仍建议您在创建索引前提前规划好以上信息,可以规避后续修改带来的不必要麻烦,若后续确有修改,您仍可以通过别名迁移的方式(推荐,可平滑过渡),或删除原索引重新创建的方式进行修改.

