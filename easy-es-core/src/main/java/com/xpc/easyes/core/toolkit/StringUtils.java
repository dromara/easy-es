package com.xpc.easyes.core.toolkit;

/**
 * 字符串工具类
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class StringUtils {
    public static final String EMPTY = "";

    private StringUtils() {
    }


    public static String concatCapitalize(String concatStr, final String str) {
        if (isEmpty(concatStr)) {
            concatStr = EMPTY;
        }
        if (str == null || str.length() == 0) {
            return str;
        }

        final char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            // already capitalized
            return str;
        }

        return concatStr + Character.toTitleCase(firstChar) + str.substring(1);
    }

    public static boolean isEmpty(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
