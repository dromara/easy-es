package org.dromara.easyes.annotation;

import java.lang.annotation.*;


/**
 * @author lyy
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EsDS {
    /**
     * 数据源名称
     *
     * @return 数据源名称
     */
    String value();
}