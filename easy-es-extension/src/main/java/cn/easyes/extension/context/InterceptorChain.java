package cn.easyes.extension.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 拦截器链
 * </p>
 *
 * @author lilu
 * @since 2022/3/4
 */
public class InterceptorChain {

    /**
     * 拦截器集合
     */
    private final List<Interceptor> interceptors = new ArrayList<>();

    /**
     * 装载拦截器
     *
     * @param t   泛型
     * @param <T> 泛型
     * @return 泛型
     */
    public <T> T pluginAll(T t) {
        for (Interceptor interceptor : interceptors) {
            t = interceptor.plugin(t);
        }
        return t;
    }

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }


    /**
     * 获取所有拦截器
     *
     * @return 拦截器集合
     */
    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }
}
