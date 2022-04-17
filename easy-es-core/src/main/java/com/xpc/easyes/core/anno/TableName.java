package com.xpc.easyes.core.anno;

import com.xpc.easyes.core.constants.BaseEsConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 索引注解
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface TableName {
    /**
     * 实体对应的索引名
     *
     * @return 默认为空
     */
    String value() default "";

    /**
     * 索引别名
     *
     * @return 别名
     */
    String aliasName() default BaseEsConstants.DEFAULT_ALIAS;

    /**
     * 是否保持使用全局的 tablePrefix 的值
     * 只生效于 既设置了全局的 tablePrefix 也设置了上面 value 的值
     * 如果是 false , 全局的 tablePrefix 不生效
     *
     * @return 默认为false
     */
    boolean keepGlobalPrefix() default false;
}
