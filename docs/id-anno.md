主键注解@TableId功能和MP一致,但相比MP针对一些低频使用功能做了一些阉割,后续根据用户反馈可随迭代逐步加入,目前版本目前只支持以下两种场景:

1. 对es中的唯一id进行重命名
1. 指定es中的唯一id生成方式

示例:
```java
public class Document {
	/**
     * es中的唯一id
     */
    @TableId(value = "myId",type = IdType.AUTO)
    private String id;
    
    // 省略其它字段...
}
```
> **Tips:**
> - **由于es对id的默认名称做了处理(下划线+id):_id,所以EE已为您屏蔽这步操作,您无需在注解中指定,框架也会自动帮您完成映射.**
> - **Id的生成类型支持以下几种:**
>   - **IdType.AUTO:** 由ES自动生成,是默认的配置,无需您额外配置 推荐
>   - **IdType.UUID:** 系统生成UUID,然后插入ES (不推荐)
>   - **IdType.CUSTOMIZE:** (版本号>=0.9.6支持)由用户自定义,用户自己对id值进行set,如果用户指定的id在es中不存在,则在insert时就会新增一条记录,如果用户指定的id在es中已存在记录,则自动更新该id对应的记录.
> - **优先级:** 注解配置的Id生成策略>全局配置的Id生成策略

