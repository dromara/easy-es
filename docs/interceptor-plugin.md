> 本插件由团队成员:璐先生贡献



**适用场景:**
需要对一些方法做特殊前置处理,比如:

- 需要在执行删除es数据操作前,需要先验证用户权限,或是记录操作记录等
- 需要在执行查询前,追加一些特殊参数,比如逻辑删除

总之就是对EE提供的各种API的AOP前置拦截增强.

**需求背景:**需要拦截本框架提供的selectList方法,然后在此方法的查询参数中追加逻辑删除为未删除的状态作为查询条件

**使用示例:**
新增拦截器,通过@Intercepts注解指定拦截的方法列表,通过@Signature注解指定被拦截的类,方法,参数等信息.
```java
@Intercepts(
        {
                @Signature(type = BaseEsMapper.class, method = "selectList", args = {LambdaEsQueryWrapper.class}),
        }
)
@Component
public class QueryInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("啊啊啊，我拦截到了查询，统一增加查询条件");
        // 查询条件中统一加入逻辑删除状态为未删除
        Object[] args = invocation.getArgs();
        LambdaEsQueryWrapper<GeneralBean> arg = (LambdaEsQueryWrapper) args[0];
        arg.eq(GeneralBean::getExistStatus, true);
        return invocation.proceed();
    }

}
```
> **Tips:**
> 1. 需要将此拦截器加上@Component注解,将其加入Spring容器,否则此拦截器不生效.
> 1. 实现的Interceptor全路径为:com.xpc.easyes.sample.interceptor,而非其它同名拦截器.


**改进建议:** 
粒度过于细,不支持通配,比如我想拦截5个方法前缀为selectXXX的方法,需要通过注解配置5次才能实现,
后续建议可支持通配拦截,比如通过select*拦截指定类中以select打头的所有方法. 后续迭代如果我有时间会优化此处,如有开发者感兴趣欢迎提交改进代码,贡献PR!
