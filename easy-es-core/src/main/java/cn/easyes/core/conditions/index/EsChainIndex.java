package cn.easyes.core.conditions.index;

import cn.easyes.core.core.EsChainWrapper;

/**
 * 链式更新方法
 * <p>
 * Copyright © 2023 xpc1024 All Rights Reserved
 **/
public interface EsChainIndex<T> extends EsChainWrapper<T> {

    /**
     * 创建索引  手动挡自定义模式 灵活度最高 使用难度稍高
     *
     * @return 是否创建成功
     */
    default Boolean createIndex() {
        return getBaseEsMapper().createIndex(getWrapper());
    }

    /**
     * 更新索引 手动挡
     *
     * @return 是否创建成功
     */
    default Boolean updateIndex() {
        return getBaseEsMapper().updateIndex(getWrapper());
    }

}
