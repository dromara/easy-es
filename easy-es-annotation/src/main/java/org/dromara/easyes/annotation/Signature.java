package org.dromara.easyes.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 签名定义
 * </p>
 *
 * @author lilu
 * @since 2022/3/4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Signature {

    /**
     * Returns the java type.
     *
     * @return the java type
     */
    Class<?> type();

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    String method();

    /**
     * Returns java types for method argument.
     *
     * @return java types for method argument
     */
    Class<?>[] args();

}
