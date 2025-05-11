package org.dromara.easyes.core.toolkit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import org.dromara.easyes.common.constants.BaseEsConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.dromara.easyes.common.constants.BaseEsConstants.LOCK_INDEX;

/**
 * 基于es写的轻量级分布式锁,仅供框架内部使用,可避免引入redis/zk等其它依赖
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public class LockUtils {
    /**
     * id字段名
     */
    private final static String ID_FIELD = "_id";
    /**
     * 重试等待时间
     */
    private final static Integer WAIT_SECONDS = 1;
    /**
     * 分布式锁内容
     */
    private final static Map<String,Object> LOCK_DOC;
    static {
        LOCK_DOC = new HashMap<>(2);
        LOCK_DOC.put("tip","Do not delete unless deadlock occurs");
    }

    /**
     * 尝试获取es分布式锁
     *
     * @param client   ElasticsearchClient
     * @param idValue  id字段值实际未entityClass名,一个entity对应一把锁
     * @param maxRetry 最大重试次数
     * @return 是否获取成功
     */
    public static synchronized boolean tryLock(ElasticsearchClient client, String idValue, Integer maxRetry) {
        boolean existsIndex = IndexUtils.existsIndex(client, LOCK_INDEX);
        if (!existsIndex) {
            IndexUtils.createEmptyIndex(client, LOCK_INDEX);
        }

        if (maxRetry <= BaseEsConstants.ZERO) {
            return Boolean.FALSE;
        }

        if (getCount(client, idValue) > BaseEsConstants.ZERO) {
            try {
                Thread.sleep(WAIT_SECONDS / maxRetry);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return tryLock(client, idValue, --maxRetry);
        } else {
            return createLock(client, idValue);
        }
    }

    /**
     * 创建锁
     *
     * @param client  ElasticsearchClient
     * @param idValue id字段值实际未entityClass名,一个entity对应一把锁
     * @return 是否创建成功
     */
    private static boolean createLock(ElasticsearchClient client, String idValue) {
        IndexRequest<?> indexRequest = IndexRequest.of(x -> x
                .index(LOCK_INDEX).id(idValue).document(LOCK_DOC)
        );
        IndexResponse response;
        try {
            response = client.index(indexRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return response.result().equals(Result.Created);
    }

    /**
     * 释放锁
     *
     * @param client   ElasticsearchClient
     * @param idValue  id字段值实际未entityClass名,一个entity对应一把锁
     * @param maxRetry 最大重试次数
     * @return 是否释放成功
     */
    public synchronized static boolean release(ElasticsearchClient client, String idValue, Integer maxRetry) {
        if (maxRetry <= BaseEsConstants.ZERO) {
            return Boolean.FALSE;
        }
        DeleteRequest deleteRequest = DeleteRequest.of(x -> x
                .index(LOCK_INDEX).id(idValue)
        );
        DeleteResponse response;
        try {
            response = client.delete(deleteRequest);
        } catch (IOException e) {
            return retryRelease(client, idValue, --maxRetry);
        }
        if (Result.Deleted.equals(response.result())) {
            return Boolean.TRUE;
        } else {
            return retryRelease(client, idValue, maxRetry);
        }
    }

    /**
     * 重试释放
     *
     * @param client   ElasticsearchClient
     * @param idValue  id字段值实际未entityClass名,一个entity对应一把锁
     * @param maxRetry 最大重试次数
     * @return 是否重试成功
     */
    private static boolean retryRelease(ElasticsearchClient client, String idValue, Integer maxRetry) {
        try {
            Thread.sleep(WAIT_SECONDS / maxRetry);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        return release(client, idValue, --maxRetry);
    }

    /**
     * 获取个数
     *
     * @param client  ElasticsearchClient
     * @param idValue id字段值实际未entityClass名,一个entity对应一把锁
     * @return 该id对应的锁的个数, 如果>0 说明已有锁,需重试获取,否则认为无锁
     */
    private static Integer getCount(ElasticsearchClient client, String idValue) {
        co.elastic.clients.elasticsearch.core.SearchRequest.Builder searchRequest = new SearchRequest.Builder();
        searchRequest.index(LOCK_INDEX);
        searchRequest.query(Query.of(x -> x.term(y -> y.field(ID_FIELD).value(idValue))));
        SearchResponse<?> response;
        try {
            response = client.search(searchRequest.build(), Object.class);
        } catch (IOException e) {
            e.printStackTrace();
            return BaseEsConstants.ONE;
        }
        return (int) response.hits().total().value();
    }

}
