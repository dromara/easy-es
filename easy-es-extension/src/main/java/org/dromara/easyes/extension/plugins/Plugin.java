package org.dromara.easyes.extension.plugins;


import org.dromara.easyes.annotation.Intercepts;
import org.dromara.easyes.annotation.Signature;
import org.dromara.easyes.common.exception.EasyEsException;
import org.dromara.easyes.common.utils.CollectionUtils;
import org.dromara.easyes.common.utils.ExceptionUtils;
import org.dromara.easyes.extension.context.Interceptor;
import org.dromara.easyes.extension.context.Invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 插件代理
 * </p>
 *
 * @author lilu
 * @since 2022/3/4
 */
public class Plugin implements InvocationHandler {

    private final Object target;
    private final Interceptor interceptor;
    private final Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    /**
     * 包装代理
     *
     * @param t           泛型
     * @param interceptor 拦截器
     * @param <T>         泛型
     * @return 泛型
     */
    @SuppressWarnings("unchecked")
    public static <T> T wrap(T t, Interceptor interceptor) {
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        return (T) Proxy.newProxyInstance(
                t.getClass().getClassLoader(),
                t.getClass().getInterfaces(),
                new Plugin(t, interceptor, signatureMap));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            if (methods != null && methods.contains(method)) {
                return interceptor.intercept(new Invocation(target, method, args));
            }
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtils.unwrapThrowable(e);
        }
    }

    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        // 检查类是否被@Intercepts标记
        if (interceptsAnnotation == null) {
            throw new RuntimeException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        Signature[] sigs = interceptsAnnotation.value();
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        // 检查被@Signature标记的方法是否存在
        for (Signature sig : sigs) {
            Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
            if (sig.useRegexp()) {
                Pattern pattern = Pattern.compile(sig.method());
                Set<Method> methodSet = Arrays.stream(sig.type().getMethods())
                        .filter(item -> pattern.matcher(item.getName()).matches())
                        .collect(Collectors.toSet());

                if (CollectionUtils.isEmpty(methodSet)) {
                    throw new EasyEsException("This regular expression does not match any methods:" + sig.type() + " named " + sig.method());
                }
                methods.addAll(methodSet);
            } else {
                try {
                    Method method = sig.type().getMethod(sig.method(), sig.args());
                    methods.add(method);
                } catch (NoSuchMethodException e) {
                    throw new EasyEsException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
                }
            }
        }
        return signatureMap;
    }
}

