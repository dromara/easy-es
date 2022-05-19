package com.xpc.easyes.core.toolkit;

import java.util.Collection;
import java.util.Map;

/**
 * 集合工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class CollectionUtils {
    public CollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }


    /**
     * 判断Map是否为空
     *
     * @param map 入参
     * @return boolean
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 判断Map是否不为空
     *
     * @param map 入参
     * @return boolean
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}
