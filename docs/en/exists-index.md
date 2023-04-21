> Can be used to verify whether there is an index with the specified name

```java
    @Test
    public void testExistsIndex(){
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean existsIndex = documentMapper.existsIndex(indexName);
        Assert.assertTrue(existsIndex);
    }
```
