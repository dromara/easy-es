```java
// 获取通过本框架生成的查询参数 可用于检验本框架生成的查询参数是否正确
String getSource(LambdaEsQueryWrapper<T> wrapper);
```
##### 参数说明
| 类型 | 参数名 | 描述 |
| --- | --- | --- |
| Wrapper<T> | updateWrapper | 实体对象封装操作类 UpdateWrapper |

> Tips:
> 生产环境可以在调用本框架查询之前,先调用此接口,将获取到的查询参数(JSONString)记录进日志,以便在出现问题时排查是否框架本身生成的查询入参有误. 当然后期稳定版本中我们会将此方法废弃,因为就目前来看存在的意义也不大,经过大量测试,框架生成的查询参数都没有发现任何问题.
> 
> 在0.9.7+版本,用户无需再手动调用此接口获取DSL语句,默认在控制台打印,生产环境如需关闭请在配置文件中配置print-dsl=false,配置可参考配置章节

