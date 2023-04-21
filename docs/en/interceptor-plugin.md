> This plugin is contributed by team member: Mr. Lu


**Applicable scene:**
Some methods require special preprocessing, such as:
● Before performing the operation of deleting es data, you need to verify user permissions or record operation records, etc.
● Need to add some special parameters before executing the query, such as logical deletion
In short, it is the enhancement of AOP pre-interception for various APIs provided by EE.

**Requirement background:** It is necessary to intercept the selectList method provided by this framework, and then append the logical deletion to the undeleted state as the query condition in the query parameters of this method

**Example of use:**
Add an interceptor, specify the intercepted method list through the @Intercepts annotation, and specify the intercepted classes, methods, parameters and other information through the @Signature annotation.

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
        System.out.println("hi selectList aop");
        // The tombstone state is uniformly added to the query condition as undeleted
        Object[] args = invocation.getArgs();
        LambdaEsQueryWrapper<GeneralBean> arg = (LambdaEsQueryWrapper) args[0];
        arg.eq(GeneralBean::getExistStatus, true);
        return invocation.proceed();
    }

}
```
> **Tips:**
> This interceptor needs to be annotated with @Component and added to the Spring container, otherwise this interceptor will not take effect.
> The full path of the implemented Interceptor is: com.xpc.easyes.sample.interceptor, not other interceptors with the same name.

**Suggestions for Improvement:**
The granularity is too fine and does not support wildcards. For example, if I want to intercept 5 methods prefixed with selectXXX, I need to configure 5 times through annotations to achieve this.
Subsequent suggestions can support wildcard interception, such as intercepting all methods starting with select in a specified class through select*. If I have time in subsequent iterations, I will optimize this place. If developers are interested, please submit improved code and contribute PR!
