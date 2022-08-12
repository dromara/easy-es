package cn.easyes.core.toolkit;

import cn.easyes.common.constants.BaseEsConstants;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

import static cn.easyes.common.constants.BaseEsConstants.LOCK_INDEX;

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
     * 尝试获取es分布式锁
     *
     * @param client   RestHighLevelClient
     * @param idValue  id字段值实际未entityClass名,一个entity对应一把锁
     * @param maxRetry 最大重试次数
     * @return 是否获取成功
     */
    public static synchronized boolean tryLock(RestHighLevelClient client, String idValue, Integer maxRetry) {
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
     * @param client  RestHighLevelClient
     * @param idValue id字段值实际未entityClass名,一个entity对应一把锁
     * @return 是否创建成功
     */
    private static boolean createLock(RestHighLevelClient client, String idValue) {
        IndexRequest indexRequest = new IndexRequest(LOCK_INDEX);
        indexRequest.id(idValue);
        indexRequest.source(BaseEsConstants.DISTRIBUTED_LOCK_TIP_JSON, XContentType.JSON);
        IndexResponse response;
        try {
            response = client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return response.status().equals(RestStatus.CREATED);
    }

    /**
     * 释放锁
     *
     * @param client   RestHighLevelClient
     * @param idValue  id字段值实际未entityClass名,一个entity对应一把锁
     * @param maxRetry 最大重试次数
     * @return 是否释放成功
     */
    public synchronized static boolean release(RestHighLevelClient client, String idValue, Integer maxRetry) {
        DeleteRequest deleteRequest = new DeleteRequest(LOCK_INDEX);
        deleteRequest.id(idValue);
        if (maxRetry <= BaseEsConstants.ZERO) {
            return Boolean.FALSE;
        }

        DeleteResponse response;
        try {
            response = client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return retryRelease(client, idValue, --maxRetry);
        }
        if (RestStatus.OK.equals(response.status())) {
            return Boolean.TRUE;
        } else {
            return retryRelease(client, idValue, maxRetry);
        }
    }

    /**
     * 重试释放
     *
     * @param client   RestHighLevelClient
     * @param idValue  id字段值实际未entityClass名,一个entity对应一把锁
     * @param maxRetry 最大重试次数
     * @return 是否重试成功
     */
    private static boolean retryRelease(RestHighLevelClient client, String idValue, Integer maxRetry) {
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
     * @param client  RestHighLevelClient
     * @param idValue id字段值实际未entityClass名,一个entity对应一把锁
     * @return 该id对应的锁的个数, 如果>0 说明已有锁,需重试获取,否则认为无锁
     */
    private static Integer getCount(RestHighLevelClient client, String idValue) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(LOCK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(ID_FIELD, idValue));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return BaseEsConstants.ONE;
        }
        return (int) response.getHits().getTotalHits().value;
    }

}
