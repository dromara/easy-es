package org.dromara.easyes.annotation;


import org.dromara.easyes.annotation.rely.HighLightTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.dromara.easyes.annotation.rely.AnnotationConstants.*;


/**
 * 字段注解
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HighLight {
    /**
     * 高亮内容映射字段的名称
     *
     * @return 高亮内容映射字段的名称
     */
    String mappingField() default "";

    /**
     * 高亮字段截取长度,默认为100
     *
     * @return 高亮字段截取长度
     */
    int fragmentSize() default DEFAULT_FRAGMENT_SIZE;

    /**
     * 搜索返回的高亮片段数量,默认全部返回
     *
     * @return 高亮片段数量
     */
    int numberOfFragments() default -1;

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

    /**
     * 使用的高亮模式
     *
     * @return 高亮模式
     */
    HighLightTypeEnum highLightType() default HighLightTypeEnum.UNIFIED;

    /**
     * 是否需要与查询字段匹配
     *
     * @return 是否需要与查询字段匹配
     */
    boolean requireFieldMatch() default true;
}
