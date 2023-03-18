package cn.easyes.core.core;

import cn.easyes.core.biz.*;
import org.elasticsearch.common.settings.Settings;
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
     * 从第多少条开始查询
     */
    protected Integer from;
    /**
     * 查询多少条记录
     */
    protected Integer size;
    /**
     * 当前操作作用的索引名数组
     */
    protected String[] indexNames;
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
    protected Wrapper<T> clone() {
        return this.clone();
    }

}
