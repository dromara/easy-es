package org.dromara.easyes.core.cache;

import org.dromara.easyes.core.config.GlobalConfig;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置缓存
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class GlobalConfigCache {

    private static final Map<Class<?>, GlobalConfig> globalConfigMap = new ConcurrentHashMap<>(1);

    public static GlobalConfig getGlobalConfig() {
        return Optional.ofNullable(globalConfigMap.get(GlobalConfig.class))
                .orElseGet(() -> {
                    GlobalConfig globalConfig = new GlobalConfig();
                    GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
                    globalConfig.setDbConfig(dbConfig);
                    return globalConfig;
                });
    }

    public static void setGlobalConfig(GlobalConfig globalConfig) {
        globalConfigMap.putIfAbsent(GlobalConfig.class, globalConfig);
    }

}
