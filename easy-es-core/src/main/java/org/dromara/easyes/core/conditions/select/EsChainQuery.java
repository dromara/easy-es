package org.dromara.easyes.core.conditions.select;


import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.biz.SAPageInfo;
import org.dromara.easyes.core.core.EsChainWrapper;

import java.util.List;
import java.util.Optional;

/**
 * 链式查询方法
 * <p>
 * Copyright © 2023 xpc1024 All Rights Reserved
 **/
public interface EsChainQuery<T> extends EsChainWrapper<T> {

    /**
     * 获取集合
     *
     * @return 集合
     */
    default List<T> list() {
        return getBaseEsMapper().selectList(getWrapper());
    }

    /**
     * 获取单个
     *
     * @return 单个
     */
    default T one() {
        return getBaseEsMapper().selectOne(getWrapper());
    }

    /**
     * 获取单个
     *
     * @return 单个
     */
    default Optional<T> oneOpt() {
        return Optional.ofNullable(one());
    }

    /**
     * 获取 count
     *
     * @return count
     */
    default Long count() {
        return (getBaseEsMapper().selectCount(getWrapper()));
    }

    /**
     * 判断数据是否存在
     *
     * @return true 存在 false 不存在
     */
    default boolean exists() {
        return this.count() > 0;
    }

    /**
     * 获取分页数据
     *
     * @param pageNum  当前页
     * @param pageSize 每页条数
     * @return 分页数据
     */
    default EsPageInfo<T> page(Integer pageNum, Integer pageSize) {
        return getBaseEsMapper().pageQuery(getWrapper(), pageNum, pageSize);
    }

    default SAPageInfo<T> searchAfterPage(List<Object> searchAfter, Integer pageSize) {
        return getBaseEsMapper().searchAfterPage(getWrapper(), searchAfter, pageSize);
    }
}
