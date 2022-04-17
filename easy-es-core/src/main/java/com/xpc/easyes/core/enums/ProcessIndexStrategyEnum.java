package com.xpc.easyes.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 索引策略枚举
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@AllArgsConstructor
public enum ProcessIndexStrategyEnum {
    /**
     * 平滑迁移策略,零停机 默认策略
     */
    SMOOTHLY(1, "smoothly"),
    /**
     * 非平滑迁移策略 简单粗暴 备选
     */
    NOT_SMOOTHLY(2, "not_smoothly"),
    /**
     * 用户手动调用API处理索引
     */
    MANUAL(3, "manual");
    @Getter
    private Integer strategyType;
    @Getter
    private String value;

    /**
     * 根据配置获取策略类型
     *
     * @param value 配置内容
     * @return 策略类型
     */
    public static Integer getStrategy(String value) {
        return Arrays.stream(ProcessIndexStrategyEnum.values())
                .filter(v -> v.getValue().equalsIgnoreCase(value))
                .findFirst()
                .map(ProcessIndexStrategyEnum::getStrategyType)
                .orElse(SMOOTHLY.strategyType);
    }
}
