package cn.easyes.core.toolkit;

import cn.easyes.core.conditions.LambdaEsIndexWrapper;
import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.conditions.LambdaEsUpdateWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * wrapper工具类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EsWrappers {
    /**
     * 获取 LambdaQueryWrapper
     *
     * @param entityClass 实体类
     * @param <T>         实体类泛型
     * @return LambdaQueryWrapper
     */
    public static <T> LambdaEsQueryWrapper<T> lambdaQuery(Class<T> entityClass) {
        return new LambdaEsQueryWrapper<>(entityClass);
    }

    /**
     * 获取 LambdaUpdateWrapper
     *
     * @param entityClass 实体类
     * @param <T>         实体类泛型
     * @return LambdaUpdateWrapper
     */
    public static <T> LambdaEsUpdateWrapper<T> lambdaUpdate(Class<T> entityClass) {
        return new LambdaEsUpdateWrapper<>(entityClass);
    }

    /**
     * 获取 LambdaEsIndexWrapper
     *
     * @param entityClass 实体类
     * @param <T>         实体类泛型
     * @return LambdaEsIndexWrapper
     */
    public static <T> LambdaEsIndexWrapper<T> lambdaIndex(Class<T> entityClass) {
        return new LambdaEsIndexWrapper<>(entityClass);
    }

}
