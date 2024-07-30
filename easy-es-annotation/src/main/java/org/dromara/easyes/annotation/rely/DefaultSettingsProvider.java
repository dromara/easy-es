package org.dromara.easyes.annotation.rely;

import java.util.Collections;
import java.util.Map;

/**
 * es 索引 默认settings 如需拓展须继承此类并覆写getSettings方法提供自定义索引settings实现
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
public class DefaultSettingsProvider implements ISettingsProvider {
    @Override
    public Map<String, Object> getSettings() {
        return Collections.emptyMap();
    }
}
