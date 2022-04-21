```java
Integer updateById(T entity);
Integer updateBatchByIds(Collection<T> entityList);
// Update records based on dynamic conditions
Integer update(T entity, LambdaEsUpdateWrapper<T> updateWrapper);
```
**Parameter Description**

| Type | Parameter name | Description |
| --- | --- | --- |
| T | entity | The entity that needs to be updated |
| Wrapper<T> | updateWrapper | Update conditions |
| Collection<T> | entityList | The entity list that needs to be updated |



