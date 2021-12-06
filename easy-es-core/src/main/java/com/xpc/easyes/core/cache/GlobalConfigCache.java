package com.xpc.easyes.core.cache;

import com.xpc.easyes.core.config.GlobalConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置缓存
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 存放配置文件中指定的全局配置信息
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class GlobalConfigCache {

    private static final Map<Class<?>, GlobalConfig> globalConfigMap = new ConcurrentHashMap<>(1);

    public static GlobalConfig getGlobalConfig() {
        return globalConfigMap.get(GlobalConfig.class);
    }

    public static void setGlobalConfig(GlobalConfig globalConfig) {
        globalConfigMap.putIfAbsent(GlobalConfig.class, globalConfig);
    }

}
