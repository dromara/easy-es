package org.dromara.easyes.extension.context;

import org.dromara.easyes.extension.plugins.Plugin;

/**
 * <p>
 * Interceptor
 * </p>
 *
 * @author lilu
 * @since 2022/3/4
 */
public interface Interceptor {

    Object intercept(Invocation invocation) throws Throwable;

    /**
     * 代理
     * @param t 泛型
     * @param <T> 泛型
     * @return 泛型
     */
    default <T> T plugin(T t) {
        return Plugin.wrap(t, this);
    }
}
