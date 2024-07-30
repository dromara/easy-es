package org.dromara.easyes.annotation.rely;

import java.util.Map;

/**
 * es 索引 settings 提供接口
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
public interface ISettingsProvider {
    /**
     * 获取settings
     *
     * @return settingsMap
     */
    Map<String, Object> getSettings();
}
