package com.xpc.easyes.core.toolkit;

/**
 * array工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class ArrayUtils {
    private ArrayUtils() {
    }

    /**
     * 判断数据是否为空
     *
     * @param array 数组
     * @return 布尔
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 数组
     * @return 布尔
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }
}
