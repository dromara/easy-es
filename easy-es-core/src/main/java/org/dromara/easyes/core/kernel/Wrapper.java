package org.dromara.easyes.core.kernel;


import lombok.SneakyThrows;
import org.dromara.easyes.core.biz.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Lambda表达式的祖宗类 存放孙子们所需的公用字段及参数
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public abstract class Wrapper<T> implements Cloneable {
    /**
     * 实体对象
     */
    protected T entity;
    /**
     * 实体类型
     */
    protected Class<T> entityClass;
    /**
     * 折叠去重字段
     */
    protected String distinctField;
    /**
     * 查询字段
     */
    protected String[] include;
    /**
     * 不查字段
     */
    protected String[] exclude;
    /**
     * 排除_score 小于 min_score 中指定的最小值的文档
     */
    protected Float minScore;
    /**
     * 自定义排序时(如 脚本里面使用 _score)，是否计算分数
     */
    protected Boolean trackScores;
    /**
     * 从第多少条开始查询
     */
    protected Integer from;
    /**
     * 查询多少条记录
     */
    protected Integer size;
    /**
     * 当前查询的查询偏好
     */
    protected String preference;
    /**
     * 当前操作作用的索引名数组
     */
    protected String[] indexNames;
    /**
     * 路由
     */
    protected String routing;
    /**
     * 参数列表
     */
    protected LinkedList<Param> paramQueue;
    /**
     * 基础排序参数列表
     */
    protected List<BaseSortParam> baseSortParams;

    /**
     * 聚合查询参数列表
     */
    protected List<AggregationParam> aggregationParamList;

    /**
     * 聚合桶排序规则列表
     */
    List<BucketOrder> bucketOrders;

    /**
     * 排序参数列表
     */
    protected List<OrderByParam> orderByParams;

    /**
     * 更新参数
     */
    protected List<EsUpdateParam> updateParamList;

    /**
     * 别名
     */
    protected String aliasName;
    /**
     * 分片数
     */
    protected Integer shardsNum;
    /**
     * 副本数
     */
    protected Integer replicasNum;
    /**
     * 最大返回数
     */
    protected Integer maxResultWindow;
    /**
     * 用户手动指定的索引mapping信息,优先级最高
     */
    protected Map<String, Object> mapping;
    /**
     * 用户手动指定的索引settings,优先级最高
     */
    protected Settings settings;
    /**
     * 索引相关参数列表
     */
    protected List<EsIndexParam> esIndexParamList;
    /**
     * 用户自定义的searchSourceBuilder 用于混合查询
     */
    protected SearchSourceBuilder searchSourceBuilder;

    /**
     * 浅拷贝当前条件构造器
     *
     * @return Wrapper
     */
    @SneakyThrows
    protected Wrapper<T> clone() {
        return (Wrapper<T>) super.clone();
    }

}
