package com.xpc.easyes.core.config;

import com.xpc.easyes.core.enums.FieldStrategy;
import com.xpc.easyes.core.enums.IdType;
import lombok.Data;

/**
 * 全局配置
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 全局配置信息
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class GlobalConfig {
    /**
     * 数据库配置
     */
    private DbConfig dbConfig;

    @Data
    public static class DbConfig {
        /**
         * 主键类型（默认 AUTO）
         */
        private IdType idType = IdType.AUTO;
        /**
         * 索引前缀
         */
        private String tablePrefix;
        /**
         * 字段验证策略 (默认 NOT NULL)
         */
        private FieldStrategy fieldStrategy = FieldStrategy.NOT_NULL;
    }
}
