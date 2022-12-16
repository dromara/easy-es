package cn.easyes.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
    SMOOTHLY(1),
    /**
     * 非平滑迁移策略 简单粗暴 备选
     */
    NOT_SMOOTHLY(2),
    /**
     * 用户手动调用API处理索引
     */
    MANUAL(3);
    @Getter
    private Integer strategyType;
}
