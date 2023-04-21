索引如果用户不配置,也不指定注解,则采用模型名称的小写字母作为索引,例如模型叫Document,那么索引就为document.<br />我们同样也支持根据@TableName注解进行指定索引名称,为了保持和MP一样的语法,这里注解命名暂时先保持@TableName,但实际上代表的是索引名称.<br />使用示例:假设我的索引名称叫: daily_document,那么我们可以在模型上加上此注解
```java
@TableName(value="daily_document",shardsNum = 3,replicasNum = 2) // 0.9.11 + 版本,索引自动托管模式下亦可通过此注解设置索引的分片数和副本数
public class Document {
    ...
}
```

>  **动态索引名称支持**
>  如果你的索引名称是不固定的,我们提供了两种方式可修改CRUD时的索引名称
>  - 调用mapper.setCurrentActiveIndex(String indexName)方法,此处的mapper为你自定义的mapper,如documentMapper,通过此API修改索引名称后,全局生效.
>  - 在对应的参数中指定当前操作作用的索引,例如 wrapper.index(String indexName),通过此API修改索引名称后,仅作用于该wrapper对应的操作,粒度最细.


> **Tips:**
> - 通过注解指定的索引名称优先级最高,指定了注解索引,则全局配置和自动生成索引不生效,采用注解中指定的索引名称.  优先级排序: 注解索引>全局配置索引前缀>自动生成
> - keepGlobalPrefix选项,(0.9.4+版本才支持)默认值为false,是否保持使用全局的 tablePrefix 的值:
>    - 既配置了全局tablePrefix,@TableName注解又指定了value值时,此注解选项才会生效,如果其值为true,则框架最终使用的索引名称为:全局tablePrefix+此注解的value,例如:dev_document.
>    - 此注解选项卡用法和MP中保持一致.
> 其中shardNum为分片数,replicasNum为副本数,如果不指定,默认值均为1

