package org.dromara.easyes.annotation.rely;

import co.elastic.clients.elasticsearch.indices.IndexSettings;

/**
 * es 索引 settings 提供接口
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
public interface ISettingsProvider {
    /**
     * 获取settings
     * @param builder 索引建造者
     */
    void settings(IndexSettings.Builder builder);
}
