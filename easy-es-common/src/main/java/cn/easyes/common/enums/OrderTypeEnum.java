package cn.easyes.common.enums;

/**
 * 排序器类型枚举
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public enum OrderTypeEnum {
    /**
     * 常规字段排序器
     */
    FIELD,
    /**
     * 用户自定义的原生语法排序器
     */
    CUSTOMIZE,
    /**
     * 得分排序器
     */
    SCORE,
    /**
     * 地理位置排序器
     */
    GEO;
}
