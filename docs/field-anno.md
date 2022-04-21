字段注解@TableField功能和MP一致,但相比MP针对一些低频使用功能做了一些阉割,后续根据用户反馈可随迭代逐步加入,截止目前最新版本已支持以下场景:

1. 实体类中的字段并非ES中实际的字段,比如把实体类直接当DTO用了,加了一些ES中并不存在的无关字段,此时可以标记此字段,以便让EE框架跳过此字段,对此字段不处理.
1. 字段的更新策略,比如在调用更新接口时,实体类的字段非Null或者非空字符串时才更新,此时可以加字段注解,对指定字段标记更新策略.
1. 对指定字段进行自定义命名,比如该字段在es中叫wu-la,但在实体model中叫ula,此时可以在value中指定value="wu-la". (0.9.8+版本支持).
1. 在自动托管索引模式下,可指定索引分词器及索引字段类型 (0.9.10+版本支持)
1. 在自动托管索引模式下,可指定索引中日期的format格式 (0.9.11+版本支持)
使用示例:
```java
    public class Document {
    // 此处省略其它字段... 
        
    // 场景一:标记es中不存在的字段
    @TableField(exist = false)
    private String notExistsField;
        
    // 场景二:更新时,此字段非空字符串才会被更新
    @TableField(strategy = FieldStrategy.NOT_EMPTY)
    private String creator;
    
    // 场景三:自定义字段名
    @TableField("wu-la")    
    private String ula;
    }

    // 场景四:支持日期字段在es索引中的format类型
    @TableField(fieldType = FieldType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private String gmtCreate;

    // 场景五:支持指定字段在es索引中的分词器类型
    @TableField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String content;
    
```
> **Tips:**
> - 更新策略一共有3种:
> 
NOT_NULL: 非Null判断,字段值为非Null时,才会被更新
> NOT_EMPTY: 非空判断,字段值为非空字符串时才会被更新
> IGNORE: 忽略判断,无论字段值为什么,都会被更新
> - 优先级: 字段注解中指定的更新策略>全局配置中指定的更新策略

>其中场景四和场景五仅在索引自动托管模式下生效,如果开启了手动处理索引模式,则需要用户通过手动调用我提供的API传入相应的分词器及日期格式化参数进行索引的创建/更新.
