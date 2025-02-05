package org.dromara.easyes.test.interceptor;

import org.dromara.easyes.annotation.Intercepts;
import org.dromara.easyes.annotation.Signature;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.kernel.BaseEsMapper;
import org.dromara.easyes.extension.context.Interceptor;
import org.dromara.easyes.extension.context.Invocation;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.springframework.stereotype.Component;

/**
 * 文件描述
 *
 * @ProductName: Hundsun HEP
 * @ProjectName: easy-es
 * @Package: org.dromara.easyes.test.interceptor
 * @Description: note
 * @Author: xingpc37977
 * @Date: 2025/2/1 19:35
 * @UpdateUser: xingpc37977
 * @UpdateDate: 2025/2/1 19:35
 * @UpdateRemark: The modified content
 * @Version: 1.0
 * <p>
 * Copyright © 2025 Hundsun Technologies Inc. All Rights Reserved
 **/

/**
 * 测试方法使用正则表达式
 *
 * @author huangjy
 */
@Intercepts(
        {
                @Signature(type = BaseEsMapper.class, method = "select.*", args = {LambdaEsQueryWrapper.class}, useRegexp = true),
                @Signature(type = BaseEsMapper.class, method = "search", args = {SearchRequest.class, RequestOptions.class}),
                @Signature(type = BaseEsMapper.class, method = "insert|update", args = {Object.class}, useRegexp = true),
                @Signature(type = BaseEsMapper.class, method = ".*ById", args = {Object.class}, useRegexp = true),
        }
)
@Component
public class TenantLineInnerInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // TODO 这里可以写你自己的拦截处理逻辑,此处仅打印
        // Object[] args = invocation.getArgs();
        // Object arg = args[0];
        // if (arg instanceof LambdaEsQueryWrapper) {
        //     LambdaEsQueryWrapper wrapper = ((LambdaEsQueryWrapper) args[0]);
        //     wrapper.eq("tenantId", "1");
        //     return invocation.proceed();
        // }
        System.out.println("增则拦截方法");
        return invocation.proceed();
    }
}
