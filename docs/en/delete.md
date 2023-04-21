```java
Integer deleteById(Serializable id);
// delete based on conditions
Integer delete(LambdaEsQueryWrapper<T> wrapper);
Integer deleteBatchIds(Collection<? extends Serializable> idList);
```
**Parameter Description**

| Type | Parameter name | Description |
| --- | --- | --- |
| Wrapper<T> | wrapper | Delete conditional packaging |
| Serializable | id | primary key in es |
| Collection<? extends Serializable> | idList | primary key list in es |



