package com.xpc.easyes.core.conditions.interfaces;

import com.xpc.easyes.core.common.PageInfo;
import com.xpc.easyes.core.conditions.LambdaEsIndexWrapper;
import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.core.conditions.LambdaEsUpdateWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 核心 所有支持方法接口
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: easy-es所有支持的方法都在此接口中
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface BaseEsMapper<T> {
    /**
     * 是否存在索引
     *
     * @param indexName
     * @return
     */
    Boolean existsIndex(String indexName);

    /**
     * 创建索引
     *
     * @param wrapper
     * @return 是否成功
     * @throws IOException
     */
    Boolean createIndex(LambdaEsIndexWrapper<T> wrapper);

    /**
     * 更新索引
     *
     * @param wrapper
     * @return
     */
    Boolean updateIndex(LambdaEsIndexWrapper<T> wrapper);

    /**
     * 删除指定索引
     *
     * @param indexName
     * @return
     */
    Boolean deleteIndex(String indexName);

    /**
     * 标准查询
     *
     * @param wrapper
     * @return
     * @throws IOException
     */
    SearchResponse search(LambdaEsQueryWrapper<T> wrapper) throws IOException;

    /**
     * 获取SearchSourceBuilder
     * 本框架生成基础查询条件,不支持的高阶语法用户可通过SearchSourceBuilder 进一步封装
     *
     * @param wrapper
     * @return
     */
    SearchSourceBuilder getSearchSourceBuilder(LambdaEsQueryWrapper<T> wrapper);
    /**
     * es原生查询
     *
     * @param searchRequest
     * @param requestOptions
     * @return
     * @throws IOException
     */
    SearchResponse search(SearchRequest searchRequest, RequestOptions requestOptions) throws IOException;

    /**
     * 获取通过本框架生成的查询参数
     * 可用于检验本框架生成的查询参数是否正确
     *
     * @param wrapper
     * @return
     */
    String getSource(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 未指定返回类型,未指定分页参数
     *
     * @param wrapper
     * @return
     * @throws IOException
     */
    PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper) throws IOException;

    /**
     * 未指定返回类型,指定分页参数
     *
     * @param wrapper
     * @param pageNum
     * @param pageSize
     * @return
     * @throws IOException
     */
    PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) throws IOException;

    /**
     * 指定返回类型,但未指定分页参数
     *
     * @param wrapper
     * @return
     * @throws IOException
     */
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 指定返回类型及分页参数
     *
     * @param wrapper
     * @param pageNum
     * @param pageSize
     * @return
     * @throws IOException
     */
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize);

    /**
     * 获取总数
     *
     * @param wrapper
     * @return
     * @throws IOException
     */
    Long selectCount(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return 成功记录数
     */
    Integer insert(T entity);

    /**
     * 批量插入
     * @param entityList
     * @return
     */
    Integer insertBatch(Collection<T> entityList);
    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     * @return 成功记录数
     */
    Integer deleteById(Serializable id);


    /**
     * 根据 entity 条件，删除记录
     *
     * @param wrapper 实体对象封装操作类（可以为 null）
     * @return 成功记录数
     */
    Integer delete(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     * @return 成功记录数
     */
    Integer deleteBatchIds(Collection<? extends Serializable> idList);

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象
     * @return 成功记录数
     */
    Integer updateById(T entity);

    /**
     * 根据ID 批量修改
     * @param entityList
     * @return
     */
    Integer updateBatchByIds(Collection<T> entityList);

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        实体对象 (set 条件值,可以为 null)
     * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     * @return 成功记录数
     */
    Integer update(T entity, LambdaEsUpdateWrapper<T> updateWrapper);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     * @return 成功记录数
     */
    T selectById(Serializable id);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     * @return 成功记录数
     */
    List<T> selectBatchIds(Collection<? extends Serializable> idList);


    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param wrapper 实体对象封装操作类（可以为 null）
     * @return 成功记录数
     */
    T selectOne(LambdaEsQueryWrapper<T> wrapper);


    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param wrapper 实体对象封装操作类（可以为 null）
     * @return 成功记录数
     */
    List<T> selectList(LambdaEsQueryWrapper<T> wrapper);
}
