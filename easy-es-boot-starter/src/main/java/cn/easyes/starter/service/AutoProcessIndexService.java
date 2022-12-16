package cn.easyes.starter.service;

import org.elasticsearch.client.RestHighLevelClient;

/**
 * 自动托管索引接口
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public interface AutoProcessIndexService {
    /**
     * 获取当前策略类型
     *
     * @return 策略类型
     */
    Integer getStrategyType();

    /**
     * 异步处理索引
     *
     * @param entityClass 实体类
     * @param client      restHighLevelClient
     */
    void processIndexAsync(Class<?> entityClass, RestHighLevelClient client);
}
