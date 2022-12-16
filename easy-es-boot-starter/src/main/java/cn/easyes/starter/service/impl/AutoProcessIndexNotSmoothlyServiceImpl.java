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

/**
 * 自动非平滑托管索引实现类, 重建索引时原索引数据会被删除
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Service
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnProperty(prefix = "easy-es", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class AutoProcessIndexNotSmoothlyServiceImpl implements AutoProcessIndexService {

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
            return doUpdateIndex(entityInfo, client);
        } else {
            // 新建
            return doCreateIndex(entityInfo, client);
        }
    }

    private boolean doUpdateIndex(EntityInfo entityInfo, RestHighLevelClient client) {
        // 获取索引信息
        EsIndexInfo esIndexInfo = IndexUtils.getIndexInfo(client, entityInfo.getRetrySuccessIndexName());

        // 索引是否有变化 若有则直接删除旧索引,创建新索引 若无则直接返回托管成功
        boolean isIndexNeedChange = IndexUtils.isIndexNeedChange(esIndexInfo, entityInfo);
        if (!isIndexNeedChange) {
            LogUtils.info("===> index has nothing changed");
            entityInfo.setIndexName(entityInfo.getRetrySuccessIndexName());
            return Boolean.TRUE;
        }

        // 直接删除旧索引
        IndexUtils.deleteIndex(client, entityInfo.getRetrySuccessIndexName());

        // 初始化创建索引参数
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo);

        // 执行创建
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

    private boolean doCreateIndex(EntityInfo entityInfo, RestHighLevelClient client) {
        // 初始化创建索引参数
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo);

        // 执行创建
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

}
