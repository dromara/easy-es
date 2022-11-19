package cn.easyes.core.config;


import cn.easyes.annotation.rely.FieldStrategy;
import cn.easyes.annotation.rely.IdType;
import cn.easyes.common.enums.ProcessIndexStrategyEnum;
import cn.easyes.common.enums.RefreshPolicy;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static cn.easyes.common.constants.BaseEsConstants.EMPTY_STR;

/**
 * easy-es全局置项
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
public class GlobalConfig {
    /**
     * whether to print dsl log 是否打印执行的dsl语句
     */
    private boolean printDsl = true;
    /**
     * process index mode Smoothly by default 索引处理模式 默认开启平滑模式
     */
    private ProcessIndexStrategyEnum processIndexMode = ProcessIndexStrategyEnum.SMOOTHLY;
    /**
     * process index blocking main thread true by default 异步处理索引是否阻塞主线程 默认阻塞
     */
    private boolean asyncProcessIndexBlocking = true;
    /**
     * is distributed environment true by default 是否分布式环境 默认为是
     */
    private boolean distributed = true;
    /**
     * Activate the current client's release index maximum number of retries
     * 分布式环境下,平滑模式,当前客户端激活最新索引最大重试次数若数据量过大,重建索引数据迁移时间超过60*(180/60)=180分钟时,可调大此参数值,此参数值决定最大重试次数,超出此次数后仍未成功,则终止重试并记录异常日志
     */
    private int activeReleaseIndexMaxRetry = 60;
    /**
     * Activate the current client's release index retry delay for a fixed time uint:second
     * 分布式环境下,平滑模式,当前客户端激活最新索引最大重试次数 若数据量过大,重建索引数据迁移时间超过60*(180/60)=180分钟时,可调大此参数值 此参数值决定多久重试一次 单位:秒
     */
    private int activeReleaseIndexFixedDelay = 180;

    /**
     * es db config 数据库配置
     */
    @NestedConfigurationProperty
    private DbConfig dbConfig = new DbConfig();

    /**
     * es db config 数据库配置
     */
    @Data
    public static class DbConfig {
        /**
         * index prefix eg:daily_, 索引前缀 可缺省
         */
        private String tablePrefix = EMPTY_STR;
        /**
         * enable underscore to camel case default false 是否开启下划线自动转驼峰 默认关闭
         */
        private boolean mapUnderscoreToCamelCase = false;
        /**
         * es id generate type. es id生成类型 默认由es自动生成
         */
        private IdType idType = IdType.NONE;
        /**
         * Field update strategy default nonNull 字段更新策略,默认非null
         */
        private FieldStrategy fieldStrategy = FieldStrategy.NOT_NULL;
        /**
         * enableTrackTotalHits default true,是否开启查询全部数据 默认开启
         */
        private boolean enableTrackTotalHits = true;
        /**
         * data refresh policy 数据刷新策略,默认为NONE
         */
        private RefreshPolicy refreshPolicy = RefreshPolicy.NONE;
        /**
         * must convert to filter must by default, must 条件转filter 默认不转换
         */
        private boolean enableMust2Filter = false;
        /**
         * Batch update threshold 10000 by default 批量更新阈值 默认值为1万
         */
        private Integer batchUpdateThreshold = 10000;
    }
}
