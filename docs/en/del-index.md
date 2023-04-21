> Delete the index, please operate with caution in the production environment, the recovery cost and difficulty are high

```java
    @Test
    public void testDeleteIndex(){
        String indexName = Document.class.getSimpleName().toLowerCase();
        boolean isOk = documentMapper.deleteIndex(indexName);
        Assert.assertTrue(isOk);
    }
```
