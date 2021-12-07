package com.xpc.easyes.core.toolkit;

import java.util.Collection;

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
}
