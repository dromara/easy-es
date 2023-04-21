```java
Long selectCount(LambdaEsQueryWrapper<T> wrapper);
T selectById(Serializable id);
List<T> selectBatchIds(Collection<? extends Serializable> idList);
// According to dynamic query conditions, query a record, if there are multiple records, will throw RuntimeException
T selectOne(LambdaEsQueryWrapper<T> wrapper);
// According to the dynamic query conditions, query all the records that meet the conditions
List<T> selectList(LambdaEsQueryWrapper<T> wrapper);
```
**Parameter Description**

| Type | Parameter name | Description |
| --- | --- | --- |
| Wrapper<T> | queryWrapper | Query parameter packaging class |
| Serializable | id | primary key in es |
| Collection<? extends Serializable> | idList | primary key list in es |


