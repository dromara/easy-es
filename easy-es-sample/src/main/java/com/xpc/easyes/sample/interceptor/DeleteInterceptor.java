package com.xpc.easyes.sample.interceptor;

import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.core.conditions.interfaces.BaseEsMapper;
import com.xpc.easyes.extension.anno.Intercepts;
import com.xpc.easyes.extension.anno.Signature;
import com.xpc.easyes.extension.plugins.Interceptor;
import com.xpc.easyes.extension.plugins.Invocation;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 统一逻辑删除拦截器 demo
 * </p>
 *
 * @author lilu
 * @since 2022/3/4
 */
@Intercepts(
        {
                // 只是个例子，如果要实现这个功能还需要拦截更多的方法
                @Signature(type = BaseEsMapper.class, method = "delete", args = {LambdaEsQueryWrapper.class}),
        }
)
@Component
public class DeleteInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 统一逻辑删除拦截
        System.out.println("啊啊啊，我拦截到了删除，禁止直接删除");
        // 这里直接改为update逻辑删除,代码省略...
        return 0;
    }

}
