package org.dromara.easyes.core.config;


import lombok.Data;
import org.dromara.easyes.annotation.rely.FieldStrategy;
import org.dromara.easyes.annotation.rely.IdType;
import org.dromara.easyes.annotation.rely.RefreshPolicy;
import org.dromara.easyes.common.enums.ProcessIndexStrategyEnum;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static org.dromara.easyes.common.constants.BaseEsConstants.EMPTY_STR;

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
     * process index mode Manual by default 索引处理模式 默认开启手动模式
     */
    private ProcessIndexStrategyEnum processIndexMode = ProcessIndexStrategyEnum.MANUAL;
    /**
     * Rebuild index timeout unit: hour, default: 72 重建索引超时时间 单位小时,默认72
     */
    private int reindexTimeOutHours = 72;
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
     * 分布式环境下,平滑模式,当前客户端激活最新索引最大重试次数,若数据量过大,重建索引数据迁移时间超过4320/60=72H,可调大此参数值,此参数值决定最大重试次数,超出此次数后仍未成功,则终止重试并记录异常日志
     */
    private int activeReleaseIndexMaxRetry = 4320;
    /**
     * Activate the current client's release index retry delay for a fixed time uint:second
     * 分布式环境下,平滑模式,当前客户端激活最新索引重试时间间隔 若您期望最终一致性的时效性更高,可调小此值,但会牺牲一些性能
     */
    private int activeReleaseIndexFixedDelay = 60;
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
        private String indexPrefix = EMPTY_STR;
        /**
         * enable underscore to camel case default false 是否开启下划线自动转驼峰 默认关闭
         */
        private boolean mapUnderscoreToCamelCase;
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
         * data refresh policy 数据刷新策略,es默认的数据刷新策略为NONE
         */
        private RefreshPolicy refreshPolicy = RefreshPolicy.NONE;
        /**
         * Batch update threshold 10000 by default 批量更新阈值 默认值为1万
         */
        private Integer batchUpdateThreshold = 10000;
        /**
         * Whether to turn on aggregation to return result set data, and turning off aggregation by default, to return only the aggregation results in the bucket can improve query efficiency. 是否开启聚合返回结果集数据,默认关闭 聚合仅返回桶中的聚合结果 可以提升查询效率
         */
        private boolean enableAggHits = false;
        /**
         * Whether to intelligently add the. keyword suffix to the field. This configuration is enabled by default. The field type is KEYWORD only for annotation configuration_ The String field of TEXT or unconfigured type takes effect and only takes effect when the query requires that the field be of keyword type, so it is called smart! 是否智能为字段添加.keyword后缀 默认开启 此配置仅对注解配置字段类型为KEYWORD_TEXT或未配置类型的String字段生效，并且只会在查询要求该字段必须为keyword类型的查询中才生效，因此谓之智能!
         */
        private boolean smartAddKeywordSuffix = true;
    }
}
