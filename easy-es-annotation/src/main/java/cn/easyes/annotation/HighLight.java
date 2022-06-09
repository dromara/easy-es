package cn.easyes.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static cn.easyes.common.constants.BaseEsConstants.HIGH_LIGHT_POST_TAG;
import static cn.easyes.common.constants.BaseEsConstants.HIGH_LIGHT_PRE_TAG;

/**
 * 字段注解
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HighLight {
    /**
     * 指定的高亮字段名称
     *
     * @return 高亮字段名称
     */
    String mappingField() default "";

    /**
     * 高亮前置标签
     *
     * @return 高亮前置标签
     */
    String preTag() default HIGH_LIGHT_PRE_TAG;

    /**
     * 高亮后置标签
     *
     * @return 高亮后置标签
     */
    String postTag() default HIGH_LIGHT_POST_TAG;
}
