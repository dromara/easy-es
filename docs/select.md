```java
	// 获取总数
    Long selectCount(LambdaEsQueryWrapper<T> wrapper);
 	// 根据 ID 查询
    T selectById(Serializable id);
	// 查询（根据ID 批量查询）
    List<T> selectBatchIds(Collection<? extends Serializable> idList);
	// 根据动态查询条件，查询一条记录 若存在多条记录 会报错
    T selectOne(LambdaEsQueryWrapper<T> wrapper);
    // 根据动态查询条件，查询全部记录
    List<T> selectList(LambdaEsQueryWrapper<T> wrapper);
```
##### 参数说明
| 类型 | 参数名 | 描述 |
| --- | --- | --- |
| Wrapper<T> | queryWrapper | 实体包装类 QueryWrapper |
| Serializable | id | 主键ID |
| Collection<? extends Serializable> | idList | 主键ID列表 |

