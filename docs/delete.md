```java
 // 根据 ID 删除
Integer deleteById(Serializable id);

// 根据 entity 条件，删除记录
Integer delete(LambdaEsQueryWrapper<T> wrapper);

// 删除（根据ID 批量删除）
Integer deleteBatchIds(Collection<? extends Serializable> idList);
```
##### 参数说明
| 类型 | 参数名 | 描述 |
| --- | --- | --- |
| Wrapper<T> | queryWrapper | 实体包装类 QueryWrapper |
| Serializable | id | 主键ID |
| Collection<? extends Serializable> | idList | 主键ID列表 |

