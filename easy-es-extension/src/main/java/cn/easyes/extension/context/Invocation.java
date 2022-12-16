package cn.easyes.extension.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>
 * 执行器
 * </p>
 *
 * @author lilu
 * @since 2022/3/4
 */
public class Invocation {

    private final Object target;
    private final Method method;
    private final Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }

}
