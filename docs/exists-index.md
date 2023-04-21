> 可用于校验是否存在名称为指定名称的索引

```java
    @Test
    public void testExistsIndex(){
        // 测试是否存在指定名称的索引
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean existsIndex = documentMapper.existsIndex(indexName);
        Assert.assertTrue(existsIndex);
    }
```
