package org.dromara.easyes.test.settings;

import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.json.JsonData;
import org.dromara.easyes.annotation.rely.DefaultSettingsProvider;

/**
 * 由于es索引的settings灵活多变,框架只能针对一部分场景作简化,其余场景需要用户自定义实现
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
public class MySettingsProvider extends DefaultSettingsProvider {

    @Override
    public void settings(IndexSettings.Builder builder) {
        builder.otherSettings("index.search.slowlog.threshold.query.warn", JsonData.of("30s"));
    }
}
