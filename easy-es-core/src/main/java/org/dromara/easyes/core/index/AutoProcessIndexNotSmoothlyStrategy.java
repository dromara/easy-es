package org.dromara.easyes.core.index;

import org.dromara.easyes.common.enums.ProcessIndexStrategyEnum;
import org.dromara.easyes.common.strategy.AutoProcessIndexStrategy;
import org.dromara.easyes.common.utils.LogUtils;
import org.dromara.easyes.core.biz.CreateIndexParam;
import org.dromara.easyes.core.biz.EntityInfo;
import org.dromara.easyes.core.biz.EsIndexInfo;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.IndexUtils;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * 自动非平滑托管索引实现类, 重建索引时原索引数据会被删除
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public class AutoProcessIndexNotSmoothlyStrategy implements AutoProcessIndexStrategy {

    @Override
    public Integer getStrategyType() {
        return ProcessIndexStrategyEnum.NOT_SMOOTHLY.getStrategyType();
    }

    @Override
    public void processIndexAsync(Class<?> entityClass, RestHighLevelClient client) {
        LogUtils.info("===> Not smoothly process index mode activated");
        IndexUtils.supplyAsync(this::process, entityClass, client);
    }

    private boolean process(Class<?> entityClass, RestHighLevelClient client) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        // 是否存在索引
        boolean existsIndex = IndexUtils.existsIndexWithRetry(entityInfo, client);
        if (existsIndex) {
            // 更新
            LogUtils.info("===> Index exists, automatically updating index by easy-es...");
            return doUpdateIndex(entityInfo, entityClass, client);
        } else {
            // 新建
            LogUtils.info("===> Index not exists, automatically creating index by easy-es...");
            return doCreateIndex(entityInfo, entityClass, client);
        }
    }

    private boolean doUpdateIndex(EntityInfo entityInfo, Class<?> clazz, RestHighLevelClient client) {
        // 获取索引信息
        EsIndexInfo esIndexInfo = IndexUtils.getIndexInfo(client, entityInfo.getRetrySuccessIndexName());

        // 索引是否有变化 若有则直接删除旧索引,创建新索引 若无则直接返回托管成功
        boolean isIndexNeedChange = IndexUtils.isIndexNeedChange(esIndexInfo, entityInfo, clazz);
        if (!isIndexNeedChange) {
            LogUtils.info("===> index has nothing changed");
            entityInfo.setIndexName(entityInfo.getRetrySuccessIndexName());
            return Boolean.TRUE;
        }

        // 直接删除旧索引
        IndexUtils.deleteIndex(client, entityInfo.getRetrySuccessIndexName());

        // 初始化创建索引参数
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo, clazz);

        // 执行创建
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

    private boolean doCreateIndex(EntityInfo entityInfo, Class<?> clazz, RestHighLevelClient client) {
        // 初始化创建索引参数
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo, clazz);

        // 执行创建
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

}
