package org.dromara.easyes.core.core;

/**
 * 链式基类
 * <p>
 * Copyright © 2023 xpc1024 All Rights Reserved
 **/
public interface EsChainWrapper<T> {

    /**
     * 获取 BaseEsMapper
     *
     * @return BaseEsMapper
     */
    BaseEsMapper<T> getBaseEsMapper();

    /**
     * 获取最终拿去执行的 Wrapper
     *
     * @return Wrapper
     */
    Wrapper<T> getWrapper();
}
