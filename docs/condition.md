> **说明:**
> - 以下出现的第一个入参boolean condition表示该条件是否加入最后生成的语句中，例如：query.like(StringUtils.isNotBlank(name), Entity::getName, name) .eq(age!=null && age >= 0, Entity::getAge, age)
> - 以下代码块内的多个方法均为从上往下补全个别boolean类型的入参,默认为true
> - 以下出现的泛型Param均为Wrapper的子类实例(均具有AbstractWrapper的所有方法)
> - 以下方法在入参中出现的R为泛型,在普通wrapper中是String,在LambdaWrapper中是函数(例:Entity::getId,Entity为实体类,getId为字段id的getMethod)
> - 以下方法入参中的R column均表示数据库字段,当R具体类型为String时则为数据库字段名(字段名是数据库关键字的自己用转义符包裹!)!而不是实体类数据字段名!!!,另当R具体类型为SFunction时项目runtime不支持eclipse自家的编译器!!!
> - 以下举例均为使用普通wrapper,入参为Map和List的均以json形式表现!
> - 使用中如果入参的Map或者List为空,则不会加入最后生成的sql中!
> - 有任何疑问就点开源码看,看不懂函数的[点击我学习新知识](https://www.jianshu.com/p/613a6118e2e0)

> **警告:**
> 不支持以及不赞成在 RPC 调用中把 Wrapper 进行传输
> 1. wrapper 很重
> 1. 传输 wrapper 可以类比为你的 controller 用 map 接收值(开发一时爽,维护火葬场)
> 1. 正确的 RPC 调用姿势是写一个 DTO 进行传输,被调用方再根据 DTO 执行相应的操作
> 1. 我们拒绝接受任何关于 RPC 传输 Wrapper 报错相关的 issue 甚至 pr

