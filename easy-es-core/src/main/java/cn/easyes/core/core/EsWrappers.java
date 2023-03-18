package cn.easyes.core.core;

import cn.easyes.core.conditions.index.LambdaEsIndexChainWrapper;
import cn.easyes.core.conditions.index.LambdaEsIndexWrapper;
import cn.easyes.core.conditions.select.LambdaEsQueryChainWrapper;
import cn.easyes.core.conditions.select.LambdaEsQueryWrapper;
import cn.easyes.core.conditions.update.LambdaEsUpdateChainWrapper;
import cn.easyes.core.conditions.update.LambdaEsUpdateWrapper;
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
     * 获取链式查询条件构造器LambdaEsQueryChainWrapper
     *
     * @param baseEsMapper 当前操作对应的mapper
     * @param <T>          泛型
     * @return LambdaEsQueryChainWrapper
     */
    public static <T> LambdaEsQueryChainWrapper<T> lambdaChainQuery(BaseEsMapper<T> baseEsMapper) {
        return new LambdaEsQueryChainWrapper<>(baseEsMapper);
    }

    /**
     * 获取链式更新条件构造器LambdaEsUpdateChainWrapper
     *
     * @param baseEsMapper 当前操作对应的mapper
     * @param <T>          泛型
     * @return LambdaEsQueryChainWrapper
     */
    public static <T> LambdaEsUpdateChainWrapper<T> lambdaChainUpdate(BaseEsMapper<T> baseEsMapper) {
        return new LambdaEsUpdateChainWrapper<>(baseEsMapper);
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

    /**
     * 获取链式LambdaEsIndexChainWrapper
     *
     * @param baseEsMapper 当前操作对应的mapper
     * @param <T>          泛型
     * @return LambdaEsIndexChainWrapper
     */
    public static <T> LambdaEsIndexChainWrapper<T> lambdaChainIndex(BaseEsMapper<T> baseEsMapper) {
        return new LambdaEsIndexChainWrapper<>(baseEsMapper);
    }
}
