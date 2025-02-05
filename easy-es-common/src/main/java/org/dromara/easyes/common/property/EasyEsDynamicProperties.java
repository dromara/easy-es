package org.dromara.easyes.common.property;

import lombok.Data;

import java.util.*;

/**
 * easy-es基础配置项 考虑到spring的场景，有些参数不是必须配置
 * 基本类型就会出现默认值的情况 所以为了要有null值出现，这里采用包装类型
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
public class EasyEsDynamicProperties {

    /**
     * 配置多动态数据源key datasource id
     */
    private Map<String, EasyEsProperties> datasource = new HashMap<>();

}
