> 删除索引,生产环境请谨慎操作,恢复代价和难度高

```java
    @Test
    public void testDeleteIndex(){
        // 测试删除索引
        // 指定要删除哪个索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean isOk = documentMapper.deleteIndex(indexName);
        Assert.assertTrue(isOk);
    }
```
