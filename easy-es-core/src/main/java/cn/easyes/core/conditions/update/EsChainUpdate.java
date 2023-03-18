package cn.easyes.core.conditions.update;

import cn.easyes.core.core.EsChainWrapper;

/**
 * 链式更新方法
 * <p>
 * Copyright © 2023 xpc1024 All Rights Reserved
 **/
public interface EsChainUpdate<T> extends EsChainWrapper<T> {

    /**
     * 更新数据
     *
     * @return 是否成功
     */
    default Integer update() {
        return update(null);
    }

    /**
     * 更新数据
     *
     * @param entity 实体类
     * @return 是否成功
     */
    default Integer update(T entity) {
        return getBaseEsMapper().update(entity, getWrapper());
    }

    /**
     * 删除数据
     *
     * @return 是否成功
     */
    default Integer remove() {
        return getBaseEsMapper().delete(getWrapper());
    }
}
