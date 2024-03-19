package org.dromara.easyes.test.settings;

import org.dromara.easyes.annotation.rely.DefaultSettingsProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 由于es索引的settings灵活多变,框架只能针对一部分场景作简化,其余场景需要用户自定义实现
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
public class MySettingsProvider extends DefaultSettingsProvider {
    @Override
    public Map<String, Object> getSettings() {
        // TODO 这里可以自定义你的settings实现,将自定义的settings置入map并返回即可
        Map<String, Object> mySettings = new HashMap<>();
        // 例如指定查询操作的慢日志阈值为30秒,当查询操作的执行时间超过此阈值时，Elasticsearch会记录相应的慢日志并发出警告
        mySettings.put("index.search.slowlog.threshold.query.warn", "30s");
        return mySettings;
    }
}
