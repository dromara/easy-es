package org.dromara.easyes.core.proxy;

import org.dromara.easyes.core.cache.BaseCache;
import org.dromara.easyes.core.core.BaseEsMapperImpl;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 代理类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class EsMapperProxy<T> implements InvocationHandler, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final int ALLOWED_MODES =
			MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE
					| MethodHandles.Lookup.PUBLIC;
	
	private static final Method privateLookupInMethod;
	
	private static final Constructor<MethodHandles.Lookup> lookupConstructor;
	
	static {
		Method privateLookupIn;
		try {
			privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
		} catch (NoSuchMethodException e) {
			privateLookupIn = null;
		}
		privateLookupInMethod = privateLookupIn;
		
		Constructor<MethodHandles.Lookup> lookup = null;
		if (privateLookupInMethod == null) {
			// JDK 1.8
			try {
				lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
				lookup.setAccessible(true);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException(
						"There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
						e);
			} catch (Exception e) {
				lookup = null;
			}
		}
		lookupConstructor = lookup;
	}
	
	private final Map<Method, EsMapperMethodInvoker> methodCache;
	
	private Class<T> mapperInterface;
	
	public EsMapperProxy(Class<T> mapperInterface, Map<Method, EsMapperMethodInvoker> methodCache) {
		this.mapperInterface = mapperInterface;
		this.methodCache = methodCache;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		BaseEsMapperImpl<?> baseEsMapperInstance = BaseCache.getBaseEsMapperInstance(mapperInterface);
		if (Object.class.equals(method.getDeclaringClass())) {
			return method.invoke(baseEsMapperInstance, args);
		}
		// 这里如果后续需要像MP那样 从xml生成代理的其它方法,则可增强method,此处并不需要
		// 增强default方法
		return cachedInvoker(method, baseEsMapperInstance).invoke(proxy, method, args);
	}
	
	private EsMapperMethodInvoker cachedInvoker(Method method, BaseEsMapperImpl<?> baseEsMapperInstance)
			throws Throwable {
		try {
			return methodCache.computeIfAbsent(method, m -> {
				if (!m.isDefault()) {
					return new PlainMethodInvoker(baseEsMapperInstance);
				}
				try {
					if (privateLookupInMethod == null) {
						return new DefaultMethodInvoker(getMethodHandleJava8(method));
					}
					return new DefaultMethodInvoker(getMethodHandleJava9(method));
				} catch (IllegalAccessException | InstantiationException | InvocationTargetException |
				         NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (RuntimeException re) {
			Throwable cause = re.getCause();
			throw cause == null ? re : cause;
		}
	}
	
	private MethodHandle getMethodHandleJava9(Method method)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final Class<?> declaringClass = method.getDeclaringClass();
		return ((MethodHandles.Lookup) privateLookupInMethod.invoke(null, declaringClass,
				MethodHandles.lookup())).findSpecial(declaringClass, method.getName(),
				MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass);
	}
	
	private MethodHandle getMethodHandleJava8(Method method)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		final Class<?> declaringClass = method.getDeclaringClass();
		return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
	}
	
	interface EsMapperMethodInvoker {
		
		Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
	}
	
	private static class PlainMethodInvoker implements EsMapperMethodInvoker {
		
		private final BaseEsMapperImpl<?> baseEsMapperInstance;
		
		public PlainMethodInvoker(BaseEsMapperImpl<?> baseEsMapperInstance) {
			this.baseEsMapperInstance = baseEsMapperInstance;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(baseEsMapperInstance, args);
		}
	}
	
	private static class DefaultMethodInvoker implements EsMapperMethodInvoker {
		
		private final MethodHandle methodHandle;
		
		public DefaultMethodInvoker(MethodHandle methodHandle) {
			this.methodHandle = methodHandle;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return methodHandle.bindTo(proxy).invokeWithArguments(args);
		}
	}
	
}
