>  **前言:** ES难用,索引首当其冲,索引的创建不仅复杂,而且难于维护,一旦索引有变动,就必须面对索引重建带来的服务停机和数据丢失等问题,尽管ES官方提供了索引别名机制来解决问题,但门槛依旧很高,步骤繁琐,在生产环境中由人工操作非常容易出现失误带来严重的问题,为了解决这些痛点,Easy-Es提供了多种策略,将用户彻底从索引的维护中解放出来.
> 其中全自动平滑模式,首次采用全球领先的"哥哥你不用动,EE我全自动"的模式,索引的创建,更新,数据迁移等所有全生命周期均无需用户介入,由EE全自动完成,过程零停机,连索引类型都可智能自动推断,一条龙服务,包您满意.是全球开源首创,充分借鉴了JVM垃圾回收算法思想,史无前例,尽管网上已有平滑过渡方案,但并非全自动,过程依旧靠人工介入,我为EE代言,请放心将索引托管给EE,索引只有在彻底迁移成功才会删除旧索引,否则均不会对原有索引和数据造成影响,发生任何意外均能保留原索引和数据,所以安全系数很高.
温馨提示:新手上路可尽量选择自动挡模式,老司机自动挡手动挡请随意~


**模式一:自动托管之平滑模式(自动挡-雪地模式) 默认开启此模式 (v0.9.10+支持)**

---

> 在此模式下,索引的创建更新数据迁移等全生命周期用户均不需要任何操作即可完成,过程零停机,用户无感知,可实现在生产环境的平滑过渡,类似汽车的自动档-雪地模式,平稳舒适,彻底解放用户,尽情享受自动架势的乐趣! 
> 需要值得特别注意的是,在自动托管模式下,系统会自动生成一条名为ee-distribute-lock的索引,该索引为框架内部使用,用户可忽略,若不幸因断电等其它因素极小概率下发生死锁,可删除该索引即可.另外,在使用时如碰到索引变更,原索引名称可能会被追加后缀_s0或_s1,不必慌张,这是全自动平滑迁移零停机的必经之路,索引后缀不影响使用,框架会自动激活该新索引.

其核心处理流程梳理如下图所示:
![平滑模式.png](https://iknow.hs.net/c6cd0fb8-93ce-437b-ac4e-36522e378d04.png)

**模式二:自动托管之非平滑模式(自动挡-运动模式)  (v0.9.10+支持)**

---

在此模式下,索引额创建及更新由EE全自动异步完成,但不处理数据迁移工作,速度极快类似汽车的自动挡-运动模式,简单粗暴,弹射起步! 适合在开发及测试环境使用,当然如果您使用logstash等其它工具来同步数据,亦可在生产环境开启此模式.

![非平滑模式.png](https://iknow.hs.net/0b1b4d41-cac5-410f-bae1-9a0b3557da75.png)

> **Tips:** 
> 以上两种自动模式中,索引信息主要依托于实体类,如果用户未对该实体类进行任何配置,EE依然能够根据字段类型智能推断出该字段在ES中的存储类型,此举可进一步减轻开发者负担,对刚接触ES的小白更是福音.

>当然,仅靠框架自动推断是不够的,我们仍然建议您在使用中尽量进行详细的配置,以便框架能自动创建出生产级的索引.举个例子,例如String类型字段,框架无法推断出您实际查询中对该字段是精确查询还是分词查询,所以它无法推断出该字段到底用keyword类型还是text类型,倘若是text类型,用户期望的分词器是什么? 这些都需要用户通过配置告诉框架,否则框架只能按默认值进行创建,届时将不能很好地完成您的期望.

>自动推断类型的优先级 < 用户通过注解指定的类型优先级


自动推断映射表:

| JAVA | ES |
| --- | --- |
| byte | byte |
| short | short |
| int | integer |
| long | long |
| float | float |
| double | double |
| BigDecimal | keyword |
| char | keyword |
| String | keyword |
| boolean | boolean |
| Date | date |
| LocalDate | date |
| LocalDateTime | date |
| List | text |
| ... | ... |


>"自动挡"模式下的最佳实践示例:

```java
@Data
@TableName(shardsNum = 3,replicasNum = 2) // 可指定分片数,副本数,若缺省则默认均为1
public class Document {
    /**
     * es中的唯一id,如果你想自定义es中的id为你提供的id,比如MySQL中的id,请将注解中的type指定为customize,如此id便支持任意数据类型)
     */
    @TableId(type = IdType.CUSTOMIZE)
    private Long id;
    /**
     * 文档标题,不指定类型默认被创建为keyword类型,可进行精确查询
     */
    private String title;
    /**
     * 文档内容,指定了类型及存储/查询分词器
     */
    @TableField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String content;
    /**
     * 作者 加@TableField注解,并指明strategy = FieldStrategy.NOT_EMPTY 表示更新的时候的策略为 创建者不为空字符串时才更新
     */
    @TableField(strategy = FieldStrategy.NOT_EMPTY)
    private String creator;
    /**
     * 创建时间
     */
    @TableField(fieldType = FieldType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private String gmtCreate;
    /**
     * es中实际不存在的字段,但模型中加了,为了不和es映射,可以在此类型字段上加上 注解@TableField,并指明exist=false
     */
    @TableField(exist = false)
    private String notExistsField;
    /**
     * 地理位置经纬度坐标 例如: "40.13933715136454,116.63441990026217"
     */
    @TableField(fieldType = FieldType.GEO_POINT)
    private String location;
    /**
     * 图形(例如圆心,矩形)
     */
    @TableField(fieldType = FieldType.GEO_SHAPE)
    private String geoLocation;
    /**
     * 自定义字段名称
     */
    @TableField(value = "wu-la")
    private String customField;

    /**
     * 高亮返回值被映射的字段
     */
    @HighLightMappingField("content")
    private String highlightContent;
}

```

**模式三:手动模式(手动挡)**

---

在此模式下,索引的所有维护工作EE框架均不介入,由用户自行处理,EE提供了开箱即用的索引[CRUD](https://www.yuque.com/laohan-14b9d/foyrfa/myborf)相关API,您可以选择使用该API手动维护索引,亦可通过es-head等工具来维护索引,总之在此模式下,您拥有更高的自由度,比较适合那些质疑EE框架的保守用户或追求极致灵活度的用户使用,类似汽车的手动挡,新手不建议使用此模式,老司机请随便.
![手动模式.png](https://iknow.hs.net/3faa18ce-c39f-44d5-b0e5-244b4828df3e.png)

**配置启用模式**

---

以上三种模式的配置,您只需要在您项目的配置文件application.properties或application.yml中加入一行配置即可:
```yaml
easy-es:
  global-config:
    process_index_mode: smoothly #smoothly:平滑模式, not_smoothly:非平滑模式, manual:手动模式
    distributed: false # 项目是否分布式环境部署,默认为true, 如果是单机运行可填false,将不加分布式锁,效率更高.
```
若缺省此行配置,则默认开启平滑模式.

> **TIPS:**
> - 以上三种模式,用户可根据实际需求灵活选择,自由体验,在使用过程中如有任何意见或建议可反馈给我们,我们将持续优化和改进,
> - EE在索引托管采用了策略+工厂设计模式,未来如果有更多更优模式,可以在不改动原代码的基础上轻松完成拓展,符合开闭原则,也欢迎各路开源爱好者贡献更多模式PR!
> - 我们将持续秉承把复杂留给框架,把易用留给用户这一理念,砥砺前行.

