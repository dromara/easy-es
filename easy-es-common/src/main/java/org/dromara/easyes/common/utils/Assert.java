package org.dromara.easyes.common.utils;

import org.dromara.easyes.common.exception.EasyEsException;

import java.util.Collection;

/**
 * 断言工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class Assert {
    private Assert() {
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new EasyEsException(message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new EasyEsException(message);
        }
    }

    public static void isEmpty(Object[] array, String message) {
        if (!isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isNotEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.isEmpty() || !containsText(str)) {
            throw new IllegalArgumentException(message);
        }
    }


    private static boolean containsText(CharSequence str) {
        int strLen = str.length();

        for (int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

}
