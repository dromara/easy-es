package org.dromara.easyes.extension.context;

/**
 * interceptorChain上下文
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public class InterceptorChainHolder {
    private final static InterceptorChainHolder INSTANCE = new InterceptorChainHolder();
    /**
     * 拦截器链
     */
    protected InterceptorChain interceptorChain;

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    public static InterceptorChainHolder getInstance() {
        return INSTANCE;
    }

    public void initInterceptorChain() {
        if (interceptorChain == null) {
            interceptorChain = new InterceptorChain();
        }
    }
}
