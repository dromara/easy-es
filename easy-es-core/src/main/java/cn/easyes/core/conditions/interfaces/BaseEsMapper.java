package cn.easyes.core.conditions.interfaces;

import cn.easyes.core.biz.PageInfo;
import cn.easyes.core.biz.SAPageInfo;
import cn.easyes.core.conditions.LambdaEsIndexWrapper;
import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.conditions.LambdaEsUpdateWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 核心 所有支持方法接口
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface BaseEsMapper<T> {
    /**
     * 是否存在索引
     *
     * @param indexName 索引名称
     * @return 返回是否存在的布尔值
     */
    Boolean existsIndex(String indexName);

    /**
     * 获取当前索引信息
     *
     * @return 当前索引信息
     */
    GetIndexResponse getIndex();

    /**
     * 获取指定索引信息
     *
     * @param indexName 指定索引名
     * @return 指定索引信息
     */
    GetIndexResponse getIndex(String indexName);

    /**
     * 创建索引,根据当前mapper对应实体类信息及其注解配置生成索引信息
     *
     * @return 是否创建成功
     */
    Boolean createIndex();

    /**
     * 创建索引
     *
     * @param wrapper 条件
     * @return 是否成功
     */
    Boolean createIndex(LambdaEsIndexWrapper<T> wrapper);

    /**
     * 更新索引
     *
     * @param wrapper 条件
     * @return 是否成功
     */
    Boolean updateIndex(LambdaEsIndexWrapper<T> wrapper);

    /**
     * 删除指定索引
     *
     * @param indexName 索引名称
     * @return 是否成功
     */
    Boolean deleteIndex(String indexName);

    /**
     * 标准查询
     *
     * @param wrapper 条件
     * @return es标准结果
     */
    SearchResponse search(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 获取SearchSourceBuilder,可用于本框架生成基础查询条件,不支持的高阶语法用户可通过SearchSourceBuilder 进一步封装
     *
     * @param wrapper 条件
     * @return 查询参数
     */
    SearchSourceBuilder getSearchSourceBuilder(LambdaEsQueryWrapper<T> wrapper);

    /**
     * es原生查询
     *
     * @param searchRequest  查询请求参数
     * @param requestOptions 类型
     * @return es原生返回结果
     * @throws IOException IO异常
     */
    SearchResponse search(SearchRequest searchRequest, RequestOptions requestOptions) throws IOException;

    /**
     * es原生滚动查询
     *
     * @param searchScrollRequest 查询请求参数
     * @param requestOptions      类型
     * @return es原生返回结果
     * @throws IOException IO异常
     */
    SearchResponse scroll(SearchScrollRequest searchScrollRequest, RequestOptions requestOptions) throws IOException;

    /**
     * 获取通过本框架生成的查询参数,可用于检验本框架生成的查询参数是否正确
     *
     * @param wrapper 条件
     * @return 查询JSON格式参数
     */
    String getSource(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 指定返回类型及分页参数
     *
     * @param wrapper  条件
     * @param pageNum  当前页
     * @param pageSize 每页条数
     * @return 指定的返回类型
     */
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize);

    /**
     * searchAfter类型分页
     *
     * @param wrapper     条件
     * @param searchAfter 当前页 第一页时为null
     * @param pageSize    每页条数
     * @return 指定的返回类型
     */
    SAPageInfo<T> searchAfterPage(LambdaEsQueryWrapper<T> wrapper, List<Object> searchAfter, Integer pageSize);

    /**
     * 获取总数 默认根据折叠参数去重
     *
     * @param wrapper 条件
     * @return 总数
     */
    default Long selectCount(LambdaEsQueryWrapper<T> wrapper) {
        return selectCount(wrapper, true);
    }

    /**
     * 是否去重获取总数
     *
     * @param wrapper  条件
     * @param distinct 是否去重
     * @return 总数
     */
    Long selectCount(LambdaEsQueryWrapper<T> wrapper, boolean distinct);

    /**
     * 插入一条记录
     *
     * @param entity 插入的数据对象
     * @return 成功条数
     */
    Integer insert(T entity);

    /**
     * 插入一条记录,可指定索引插入
     *
     * @param entity    插入的数据对象
     * @param indexName 指定插入的索引名
     * @return 成功条数
     */
    Integer insert(T entity, String indexName);

    /**
     * 批量插入
     *
     * @param entityList 插入的数据对象列表
     * @return 总成功条数
     */
    Integer insertBatch(Collection<T> entityList);

    /**
     * 批量插入
     *
     * @param entityList 插入的数据对象列表
     * @param indexName  指定插入的索引名
     * @return 总成功条数
     */
    Integer insertBatch(Collection<T> entityList, String indexName);

    /**
     * 根据 ID 删除
     *
     * @param id 主键
     * @return 成功条数
     */
    Integer deleteById(Serializable id);

    /**
     * 根据 ID 删除
     *
     * @param id        主键
     * @param indexName 指定删除的索引名
     * @return 成功条数
     */
    Integer deleteById(Serializable id, String indexName);

    /**
     * 根据 entity 条件，删除记录
     *
     * @param wrapper 条件
     * @return 总成功条数
     */
    Integer delete(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键列表
     * @return 总成功条数
     */
    Integer deleteBatchIds(Collection<? extends Serializable> idList);

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList    主键列表
     * @param indexName 指定删除的索引名
     * @return 总成功条数
     */
    Integer deleteBatchIds(Collection<? extends Serializable> idList, String indexName);

    /**
     * 根据 ID 更新
     *
     * @param entity 更新对象
     * @return 总成功条数
     */
    Integer updateById(T entity);

    /**
     * 根据 ID 更新
     *
     * @param entity    更新对象
     * @param indexName 指定更新的索引名称
     * @return 总成功条数
     */
    Integer updateById(T entity, String indexName);

    /**
     * 根据ID 批量更新
     *
     * @param entityList 更新对象列表
     * @return 总成功条数
     */
    Integer updateBatchByIds(Collection<T> entityList);

    /**
     * 根据ID 批量更新
     *
     * @param entityList 更新对象列表
     * @param indexName  指定更新的索引名称
     * @return 总成功条数
     */
    Integer updateBatchByIds(Collection<T> entityList, String indexName);

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        更新对象
     * @param updateWrapper 条件
     * @return 成功条数
     */
    Integer update(T entity, LambdaEsUpdateWrapper<T> updateWrapper);

    /**
     * 根据 ID 查询
     *
     * @param id 主键
     * @return 指定的返回对象
     */
    T selectById(Serializable id);

    /**
     * 根据 ID 查询
     *
     * @param id        主键
     * @param indexName 指定查询的索引名
     * @return 指定的返回对象
     */
    T selectById(Serializable id, String indexName);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键列表
     * @return 指定的返回对象列表
     */
    List<T> selectBatchIds(Collection<? extends Serializable> idList);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList    主键列表
     * @param indexName 指定查询的索引名
     * @return 指定的返回对象列表
     */
    List<T> selectBatchIds(Collection<? extends Serializable> idList, String indexName);

    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param wrapper 条件
     * @return 指定的返回对象
     */
    T selectOne(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param wrapper 条件
     * @return 指定的返回对象列表
     */
    List<T> selectList(LambdaEsQueryWrapper<T> wrapper);

    /**
     * 设置当前Mapper默认激活的全局索引名称 务必谨慎操作,设置后全局生效,永驻jvm,除非项目重启
     *
     * @param indexName 索引名称
     * @return 是否成功
     */
    Boolean setCurrentActiveIndex(String indexName);
}
