```java
enableMust2Filter(boolean enable)
enableMust2Filter(boolean condition, boolean enable)
```

> 是否将must查询条件转换成filter查询条件,可以在wrapper中直接指定本次查询的条件是否转换,如果不指定,则从全局配置文件中获取,若配置文件中也未配置,则默认不转换.
> must查询条件计算得分,filter不计算得分,因此在不需要计算得分的查询场景中,开启此配置可提升少许查询性能.