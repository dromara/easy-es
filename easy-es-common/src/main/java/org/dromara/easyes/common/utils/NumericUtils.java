package org.dromara.easyes.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数字工具类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumericUtils {
    /**
     * 设置保留小数位, 四舍五入
     *
     * @param origin        原始值
     * @param decimalPlaces 保留位数
     * @return 目标值
     */
    public static float setDecimalPlaces(float origin, int decimalPlaces) {
        return BigDecimal.valueOf(origin)
                .setScale(decimalPlaces, RoundingMode.HALF_UP)
                .floatValue();
    }

    /**
     * 设置保留小数位, 四舍五入
     *
     * @param origin        原始值
     * @param decimalPlaces 保留位数
     * @return 目标值
     */
    public static double setDecimalPlaces(double origin, int decimalPlaces) {
        return BigDecimal.valueOf(origin)
                .setScale(decimalPlaces, RoundingMode.HALF_UP)
                .doubleValue();
    }


    /**
     * 整数转double,保留1为小数 例如输入10,输出10.0
     *
     * @param number 原始数值
     * @return 保留1位小数后的数值
     */
    public static double formatNumberWithOneDecimal(int number) {
        return Math.round(number * 10.0) / 10.0;
    }
}
