package org.dromara.easyes.common.utils;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * fastjson 工具类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FastJsonUtils {
    /**
     * 设置fastjson toJsonString字段
     *
     * @param clazz  类
     * @param fields 字段列表
     * @return 前置过滤器
     */
    public static SimplePropertyPreFilter getSimplePropertyPreFilter(Class<?> clazz, Set<String> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        SimplePropertyPreFilter simplePropertyPreFilter = new SimplePropertyPreFilter(clazz);
        fields.forEach(field -> simplePropertyPreFilter.getExcludes().add(field));
        return simplePropertyPreFilter;
    }


}
