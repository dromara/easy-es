```java
// 插入一条记录
Integer insert(T entity);

// 批量插入多条记录
Integer insertBatch(Collection<T> entityList)
```
##### 参数说明
| 类型 | 参数名 | 描述 |
| --- | --- | --- |
| T | entity | 实体对象 |
| Collection<T> | entityList | 实体对象集合 |

> **Tips:**插入后如需id值可直接从entity中取,用法和MP中一致,批量插入亦可直接从原对象中获取插入成功后的数据id,以上接口返回Integer为成功条数

