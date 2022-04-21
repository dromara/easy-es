```java
//根据 ID 更新
Integer updateById(T entity);

// 根据ID 批量更新
Integer updateBatchByIds(Collection<T> entityList);

//  根据动态条件 更新记录
Integer update(T entity, LambdaEsUpdateWrapper<T> updateWrapper);
```
##### 参数说明
| 类型 | 参数名 | 描述 |
| --- | --- | --- |
| T | entity | 实体对象 |
| Wrapper<T> | updateWrapper | 实体对象封装操作类 UpdateWrapper |
| Collection<T> | entityList | 实体对象集合 |

