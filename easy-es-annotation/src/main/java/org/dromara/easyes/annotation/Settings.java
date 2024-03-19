package org.dromara.easyes.annotation;

import org.dromara.easyes.annotation.rely.DefaultSettingsProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.dromara.easyes.annotation.rely.AnnotationConstants.*;

/**
 * es 索引 settings
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Settings {
    /**
     * 分片数
     *
     * @return 默认为1
     */
    int shardsNum() default DEFAULT_SHARDS;

    /**
     * 副本数
     *
     * @return 默认为1
     */
    int replicasNum() default DEFAULT_REPLICAS;

    /**
     * 默认最大返回数
     *
     * @return 默认1w条
     */
    int maxResultWindow() default DEFAULT_MAX_RESULT_WINDOW;

    /**
     * 索引的刷新间隔 es默认值为1s ms：表示毫秒 s：表示秒 m：表示分钟
     *
     * @return 索引的刷新间隔
     */
    String refreshInterval() default "";

    /**
     * 自定义settings提供类
     *
     * @return 自定义settings提供类 默认为DefaultSettingsProvider空实现 如需自定义,可继承此类并覆写getSettings方法 将settings信息以Map返回
     */
    Class<? extends DefaultSettingsProvider> settingsProvider() default DefaultSettingsProvider.class;
}
