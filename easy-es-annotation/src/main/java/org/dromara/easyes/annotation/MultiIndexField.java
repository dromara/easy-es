package org.dromara.easyes.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多字段索引注解
 * <p>
 *
 * @author yinlei
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MultiIndexField {
    /**
     * 主字段信息
     *
     * @return 主字段信息
     */
    IndexField mainIndexField();

    /**
     * 内部字段信息
     *
     * @return 内部字段信息
     */
    InnerIndexField[] otherIndexFields() default {};
}
