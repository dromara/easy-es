package com.xpc.easyes.core.enums;

import lombok.AllArgsConstructor;

/**
 * 分词器枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum Analyzer {
    STANDARD,
    SIMPLE,
    STOP,
    WHITESPACE,
    KEYWORD,
    PATTERN,
    LANGUAGE,
    SNOWBALL,
    IK_SMART,
    IK_MAX_WORD,
    PINYIN,
}
