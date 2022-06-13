**基础配置:**<br />如果缺失可导致项目无法正常启动,其中账号密码可缺省.
```yaml
easy-es:
  enable: true # 是否开启EE自动配置
  address : 127.0.0.1:9200 # es连接地址+端口 格式必须为ip:port,如果是集群则可用逗号隔开
  schema: http # 默认为http
  username: elastic #如果无账号密码则可不配置此行
  password: WG7WVmuNMtM4GwNYkyWH #如果无账号密码则可不配置此行
```
**拓展配置:**<br />可缺省,为了提高生产性能,你可以进一步配置(0.9.4+版本才支持)
```yaml
easy-es:
  keep-alive-millis: 18000 # 心跳策略时间 单位:ms
  connectTimeout: 5000 # 连接超时时间 单位:ms
  socketTimeout: 5000 # 通信超时时间 单位:ms
  requestTimeout: 5000 # 请求超时时间 单位:ms
  connectionRequestTimeout: 5000 # 连接请求超时时间 单位:ms
  maxConnTotal: 100 # 最大连接数 单位:个
  maxConnPerRoute: 100 # 最大连接路由数 单位:个
```
**全局配置:**<br />可缺省,不影响项目启动,若缺省则为默认值
```yaml
easy-es:
  global-config:
    process_index_mode: smoothly #索引处理模式(0.9.10+版本支持),smoothly:平滑模式,默认开启此模式, not_smoothly:非平滑模式, manual:手动模式
    print-dsl: true # 开启控制台打印通过本框架生成的DSL语句,默认为开启,生产环境建议关闭(0.9.7+版本支持)
    distributed: false # 当前项目是否分布式项目(0.9.10+版本支持),默认为true,在非手动托管索引模式下,若为分布式项目则会获取分布式锁,非分布式项目只需synchronized锁.
    db-config:
      map-underscore-to-camel-case: false # 是否开启下划线转驼峰 默认为false(0.9.8+版本支持)
      table-prefix: daily_ # 索引前缀,可用于区分环境  默认为空 用法和MP一样
      id-type: customize # id生成策略 customize为自定义,id值由用户生成,比如取MySQL中的数据id,如缺省此项配置,则id默认策略为es自动生成
      field-strategy: not_empty # 字段更新策略 默认为not_null
      enable-track-total-hits: true # 默认开启,查询若指定了size超过1w条时也会自动开启,开启后查询所有匹配数据,若不开启,会导致无法获取数据总条数,其它功能不受影响.
      refresh-policy: immediate # 数据刷新策略,默认为不刷新
      enable-must2-filter: false # 是否全局开启must查询类型转换为filter查询类型 默认为false不转换 

```

**其它配置:**
```yaml
logging:
  level:
   tracer: trace # 开启trace级别日志,在开发时可以开启此配置,则控制台可以打印es全部请求信息及DSL语句,为了避免重复,开启此项配置后,可以将EE的print-dsl设置为false.

spring:
  main:
    banner-mode: off # 有用户反馈想关闭EE打印的Banner,由于EE的banner是直接覆盖springboot默认banner的,所以如需关闭,直接关闭springboot的banner即可关闭EE的banner.
```

> **Tips:**
> - id-type支持3种类型:
>     - auto: 由ES自动生成,是默认的配置,无需您额外配置 推荐
>     - uuid: 系统生成UUID,然后插入ES (不推荐)
>     - customize: 用户自定义,在此类型下,用户可以将任意数据类型的id存入es作为es中的数据id,比如将mysql自增的id作为es的id,可以开启此模式,或通过@TableId(type)注解指定.

> - field-strategy支持3种类型:
>     - not_null: 非Null判断,字段值为非Null时,才会被更新
>     - not_empty: 非空判断,字段值为非空字符串时才会被更新
>     - ignore: 忽略判断,无论字段值为什么,都会被更新
>     - 在配置了全局策略后,您仍可以通过注解针对个别类进行个性化配置,全局配置的优先级是小于注解配置的
> - refresh-policy支持3种策略
>     - none: 默认策略,不刷新数据
>     - immediate : 立即刷新,会损耗较多性能,对数据实时性要求高的场景下适用
>     - wait_until: 请求提交数据后，等待数据完成刷新(1s)，再结束请求 性能损耗适中
