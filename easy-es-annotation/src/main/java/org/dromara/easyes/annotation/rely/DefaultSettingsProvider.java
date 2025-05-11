package org.dromara.easyes.annotation.rely;

import co.elastic.clients.elasticsearch.indices.IndexSettings;

/**
 * es 索引 默认settings 如需拓展须继承此类并覆写getSettings方法提供自定义索引settings实现
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
public class DefaultSettingsProvider implements ISettingsProvider {
    @Override
    public void settings(IndexSettings.Builder builder) {
    }
}
