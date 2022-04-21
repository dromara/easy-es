在很多场景下,由于实体模型或需求的变动,我们需要对索引进行更新,例如,我在文档实体模型中新增了一个作者字段creator,此时我想更新索引,以便能够根据作者关键字进行搜索:
```java
    @Test
    public void testUpdateIndex(){
        // 测试更新索引
        LambdaEsIndexWrapper<Document> wrapper = new LambdaEsIndexWrapper<>();
        // 指定要更新哪个索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        wrapper.indexName(indexName);
        wrapper.mapping(Document::getCreator,FieldType.KEYWORD);
        boolean isOk = documentMapper.updateIndex(wrapper);
        Assert.assertTrue(isOk);
    }
```
> **Tips:**
> - 如果您的生产环境需要平滑过渡,那么我们不建议用此方式更新索引,因为更新mapping会导致Es重建索引,此类情形下,建议通过别名alias的方式进行迁移
> - indexName不能为空,必须指定要更新哪个索引
> - 本接口仅支持更新索引的mapping,如需更新分片,数据集,索引名称等信息,建议可调用删除索引接口删除原索引,然后调用索引创建接口重新创建索引(低频操作,后期版本可根据用户反馈决定是否加入支持更新)

