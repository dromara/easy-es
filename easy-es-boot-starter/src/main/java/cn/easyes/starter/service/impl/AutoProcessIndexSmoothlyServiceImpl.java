package cn.easyes.starter.service.impl;

import cn.easyes.common.enums.ProcessIndexStrategyEnum;
import cn.easyes.common.utils.LogUtils;
import cn.easyes.core.biz.CreateIndexParam;
import cn.easyes.core.biz.EntityInfo;
import cn.easyes.core.biz.EsIndexInfo;
import cn.easyes.core.toolkit.EntityInfoHelper;
import cn.easyes.core.toolkit.IndexUtils;
import cn.easyes.starter.service.AutoProcessIndexService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static cn.easyes.common.constants.BaseEsConstants.S1_SUFFIX;
import static cn.easyes.common.constants.BaseEsConstants.SO_SUFFIX;

/**
 * 自动平滑托管索引实现类,本框架默认模式,过程零停机,数据会自动转移至新索引
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Service
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnProperty(prefix = "easy-es", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class AutoProcessIndexSmoothlyServiceImpl implements AutoProcessIndexService {
    @Override
    public Integer getStrategyType() {
        return ProcessIndexStrategyEnum.SMOOTHLY.getStrategyType();
    }

    @Override
    public void processIndexAsync(Class<?> entityClass, RestHighLevelClient client) {
        LogUtils.info("===> Smoothly process index mode activated");
        IndexUtils.supplyAsync(this::process, entityClass, client);
    }


    private synchronized boolean process(Class<?> entityClass, RestHighLevelClient client) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);

        // 索引是否已存在
        boolean existsIndex = IndexUtils.existsIndexWithRetryAndSetActiveIndex(entityInfo, client);
        if (existsIndex) {
            // 更新
            return doUpdateIndex(entityInfo, client);
        } else {
            // 新建
            return doCreateIndex(entityInfo, client);
        }
    }


    private boolean doUpdateIndex(EntityInfo entityInfo, RestHighLevelClient client) {
        // 获取索引信息
        EsIndexInfo esIndexInfo = IndexUtils.getIndexInfo(client, entityInfo.getIndexName());

        // 是否存在默认别名,若无则给添加
        if (!esIndexInfo.getHasDefaultAlias()) {
            IndexUtils.addDefaultAlias(client, entityInfo.getIndexName());
        }

        // 索引是否有变化 若有则创建新索引并无感迁移, 若无则直接返回托管成功
        boolean isIndexNeedChange = IndexUtils.isIndexNeedChange(esIndexInfo, entityInfo);
        if (!isIndexNeedChange) {
            LogUtils.info("===> index has nothing changed");
            return Boolean.TRUE;
        }

        // 创建新索引
        String releaseIndexName = generateReleaseIndexName(entityInfo.getIndexName());
        entityInfo.setReleaseIndexName(releaseIndexName);
        boolean isCreateIndexSuccess = doCreateIndex(entityInfo, client);
        if (!isCreateIndexSuccess) {
            LogUtils.error("create release index failed", "releaseIndex:" + releaseIndexName);
            return Boolean.FALSE;
        }

        //  迁移数据至新创建的索引
        boolean isDataMigrationSuccess = doDataMigration(entityInfo.getIndexName(), releaseIndexName, client);
        if (!isDataMigrationSuccess) {
            LogUtils.error("migrate data failed", "oldIndex:" + entityInfo.getIndexName(), "releaseIndex:" + releaseIndexName);
            return Boolean.FALSE;
        }

        // 原子操作 切换别名:将默认别名关联至新索引,并将旧索引的默认别名移除
        boolean isChangeAliasSuccess = IndexUtils.changeAliasAtomic(client, entityInfo.getIndexName(), releaseIndexName);
        if (!isChangeAliasSuccess) {
            LogUtils.error("change alias atomically failed", "oldIndex:" + entityInfo.getIndexName(), "releaseIndex:" + releaseIndexName);
            return Boolean.FALSE;
        }

        // 删除旧索引
        boolean isDeletedIndexSuccess = IndexUtils.deleteIndex(client, entityInfo.getIndexName());
        if (!isDeletedIndexSuccess) {
            LogUtils.error("delete old index failed", "oldIndex:" + entityInfo.getIndexName());
            return Boolean.FALSE;
        }

        // 用最新索引覆盖缓存中的老索引
        entityInfo.setIndexName(releaseIndexName);

        // 将新索引名称记录至ee-distribute-lock索引中,以便在分布式环境下其它机器能够感知到
        IndexUtils.saveReleaseIndex(releaseIndexName,client);

        // done.
        return Boolean.TRUE;
    }

    private String generateReleaseIndexName(String oldIndexName) {
        if (oldIndexName.endsWith(SO_SUFFIX)) {
            return oldIndexName.split(SO_SUFFIX)[0] + S1_SUFFIX;
        } else if (oldIndexName.endsWith(S1_SUFFIX)) {
            return oldIndexName.split(S1_SUFFIX)[0] + SO_SUFFIX;
        } else {
            return oldIndexName + SO_SUFFIX;
        }
    }

    private boolean doDataMigration(String oldIndexName, String releaseIndexName, RestHighLevelClient client) {
        return IndexUtils.reindex(client, oldIndexName, releaseIndexName);
    }

    private boolean doCreateIndex(EntityInfo entityInfo, RestHighLevelClient client) {
        // 初始化创建索引参数
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo);
        // 执行创建
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

}
