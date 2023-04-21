package org.dromara.easyes.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 得分注解
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Score {
    /**
     * 保留小数位,默认不处理,不处理es得分,效率更高
     *
     * @return 保留小数位
     */
    int decimalPlaces() default 0;
}
