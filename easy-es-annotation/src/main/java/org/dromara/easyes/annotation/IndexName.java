package org.dromara.easyes.annotation;

import org.dromara.easyes.annotation.rely.RefreshPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.dromara.easyes.annotation.rely.AnnotationConstants.DEFAULT_ALIAS;

/**
 * 索引注解
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface IndexName {
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
    String aliasName() default DEFAULT_ALIAS;

    /**
     * 是否保持使用全局的 tablePrefix 的值
     * 只生效于 既设置了全局的 tablePrefix 也设置了上面 value 的值
     * 如果是 false , 全局的 tablePrefix 不生效
     *
     * @return 默认为false
     */
    boolean keepGlobalPrefix() default false;

    /**
     * 数据刷新策略
     *
     * @return 具体策略
     */
    RefreshPolicy refreshPolicy() default RefreshPolicy.GLOBAL;
}
