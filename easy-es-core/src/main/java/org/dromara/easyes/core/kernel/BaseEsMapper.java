package org.dromara.easyes.core.kernel;

import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.biz.SAPageInfo;
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
     * 获取mapper中的entityClass
     *
     * @return entityClass
     */
    Class<T> getEntityClass();

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
     * 创建索引,根据当前mapper对应实体类信息及其注解配置生成索引信息 可指定索引名进行创建 适用于定时任务按日期创建索引场景
     *
     * @param indexName 指定的索引名,会覆盖注解上指定的索引名
     * @return 是否创建成功
     */
    Boolean createIndex(String indexName);

    /**
     * 创建索引
     *
     * @param wrapper 条件
     * @return 是否成功
     */
    Boolean createIndex(Wrapper<T> wrapper);

    /**
     * 更新索引
     *
     * @param wrapper 条件
     * @return 是否成功
     */
    Boolean updateIndex(Wrapper<T> wrapper);

    /**
     * 删除指定索引
     *
     * @param indexNames 索引名称数组
     * @return 是否成功
     */
    Boolean deleteIndex(String... indexNames);

    /**
     * 刷新索引
     *
     * @return 刷新成功分片总数
     * @author 社区roin贡献 ee作者整合提交
     */
    Integer refresh();

    /**
     * 批量刷新指定索引列表
     *
     * @param indexNames 索引名称
     * @return 刷新成功分片总数
     * @author 社区roin贡献 ee作者整合提交
     */
    Integer refresh(String... indexNames);

    /**
     * 执行SQL语句
     *
     * @param sql 被执行的sql语句
     * @return 执行结果 jsonString
     */
    String executeSQL(String sql);

    /**
     * 执行静态dsl语句 不传索引名,默认为当前mapper对应索引
     *
     * @param dsl dsl语句
     * @return 执行结果 jsonString
     */
    String executeDSL(String dsl);

    /**
     * 执行静态dsl语句 可指定作用的索引
     *
     * @param dsl       dsl语句
     * @param indexName 作用的索引名
     * @return 执行结果 jsonString
     */
    String executeDSL(String dsl, String indexName);

    /**
     * 混合查询
     *
     * @param wrapper 条件
     * @return es标准结果
     */
    SearchResponse search(Wrapper<T> wrapper);

    /**
     * 获取SearchSourceBuilder,可用于本框架生成基础查询条件,不支持的高阶语法用户可通过SearchSourceBuilder 进一步封装
     *
     * @param wrapper 条件
     * @return 查询参数
     */
    SearchSourceBuilder getSearchSourceBuilder(Wrapper<T> wrapper);

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
    String getSource(Wrapper<T> wrapper);

    /**
     * 指定返回类型及分页参数
     *
     * @param wrapper  条件
     * @param pageNum  当前页
     * @param pageSize 每页条数
     * @return 指定的返回类型
     */
    EsPageInfo<T> pageQuery(Wrapper<T> wrapper, Integer pageNum, Integer pageSize);

    /**
     * searchAfter类型分页
     *
     * @param wrapper     条件
     * @param searchAfter 当前页 第一页时为null
     * @param pageSize    每页条数
     * @return 指定的返回类型
     */
    SAPageInfo<T> searchAfterPage(Wrapper<T> wrapper, List<Object> searchAfter, Integer pageSize);

    /**
     * 获取总数(智能推断:若wrapper中指定了去重字段则去重,若未指定则不去重 推荐使用)
     *
     * @param wrapper 条件
     * @return 总数
     */
    Long selectCount(Wrapper<T> wrapper);


    /**
     * 无论wrapper中是否指定去重字段,都以用户传入的distinct布尔值作为是否去重的条件
     *
     * @param wrapper  条件
     * @param distinct 是否去重
     * @return 总数
     */
    Long selectCount(Wrapper<T> wrapper, boolean distinct);

    /**
     * 插入一条记录
     *
     * @param entity 插入的数据对象
     * @return 成功条数
     */
    Integer insert(T entity);

    /**
     * 插入一条记录 可指定路由
     *
     * @param routing 路由
     * @param entity  插入的数据对象
     * @return 成功条数
     */
    Integer insert(String routing, T entity);

    /**
     * 父子类型 插入一条记录 可指定路由, 父id
     *
     * @param routing  路由
     * @param parentId 父id
     * @param entity   插入的数据对象
     * @return 成功条数
     */
    Integer insert(String routing, String parentId, T entity);

    /**
     * 插入一条记录,可指定多索引插入
     *
     * @param entity     插入的数据对象
     * @param indexNames 指定插入的索引名数组
     * @return 总成功条数
     */
    Integer insert(T entity, String... indexNames);

    /**
     * 插入数据,可指定路由及多索引插入
     *
     * @param routing    路由
     * @param entity     插入的数据对象
     * @param indexNames 指定插入的索引名数组
     * @return 总成功条数
     */
    Integer insert(String routing, T entity, String... indexNames);

    /**
     * 父子类型 插入数据,可指定路由,父id及多索引插入
     *
     * @param routing    路由
     * @param parentId   父id
     * @param entity     插入的数据对象
     * @param indexNames 指定插入的索引名数组
     * @return 总成功条数
     */
    Integer insert(String routing, String parentId, T entity, String... indexNames);

    /**
     * 批量插入
     *
     * @param entityList 插入的数据对象列表
     * @return 总成功条数
     */
    Integer insertBatch(Collection<T> entityList);

    /**
     * 批量插入 可指定路由
     *
     * @param routing    路由
     * @param entityList 插入的数据对象列表
     * @return 总成功条数
     */
    Integer insertBatch(String routing, Collection<T> entityList);

    /**
     * 父子类型 批量插入 可指定路由, 父id
     *
     * @param routing    路由
     * @param parentId   父id
     * @param entityList 插入的数据对象列表
     * @return 总成功条数
     */
    Integer insertBatch(String routing, String parentId, Collection<T> entityList);

    /**
     * 批量插入 可指定多索引
     *
     * @param entityList 插入的数据对象列表
     * @param indexNames 指定插入的索引名数组
     * @return 总成功条数
     */
    Integer insertBatch(Collection<T> entityList, String... indexNames);

    /**
     * 批量插入 可指定路由及多索引
     *
     * @param routing    路由
     * @param entityList 插入的数据对象列表
     * @param indexNames 指定插入的索引名数组
     * @return 总成功条数
     */
    Integer insertBatch(String routing, Collection<T> entityList, String... indexNames);

    /**
     * 父子类型 批量插入 可指定路由,父id及多索引
     *
     * @param routing    路由
     * @param parentId   父id
     * @param entityList 插入的数据对象列表
     * @param indexNames 指定插入的索引名数组
     * @return 总成功条数
     */
    Integer insertBatch(String routing, String parentId, Collection<T> entityList, String... indexNames);

    /**
     * 根据 ID 删除
     *
     * @param id 主键
     * @return 成功条数
     */
    Integer deleteById(Serializable id);

    /**
     * 根据 ID 删除 可指定路由
     *
     * @param routing 路由
     * @param id      主键
     * @return 成功条数
     */
    Integer deleteById(String routing, Serializable id);

    /**
     * 根据 ID 删除 可指定多索引
     *
     * @param id         主键
     * @param indexNames 指定删除的索引名数组
     * @return 总成功条数
     */
    Integer deleteById(Serializable id, String... indexNames);

    /**
     * 根据 ID 删除 可指定路由及多索引
     *
     * @param routing    路由
     * @param id         主键
     * @param indexNames 指定删除的索引名数组
     * @return 总成功条数
     */
    Integer deleteById(String routing, Serializable id, String... indexNames);


    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键列表
     * @return 总成功条数
     */
    Integer deleteBatchIds(Collection<? extends Serializable> idList);

    /**
     * 删除（根据ID 批量删除）可指定路由
     *
     * @param routing 路由
     * @param idList  主键列表
     * @return 总成功条数
     */
    Integer deleteBatchIds(String routing, Collection<? extends Serializable> idList);

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList     主键列表
     * @param indexNames 指定删除的索引名数组
     * @return 总成功条数
     */
    Integer deleteBatchIds(Collection<? extends Serializable> idList, String... indexNames);

    /**
     * 删除（根据ID 批量删除） 可指定路由及多索引
     *
     * @param routing    路由
     * @param idList     主键列表
     * @param indexNames 指定删除的索引名数组
     * @return 总成功条数
     */
    Integer deleteBatchIds(String routing, Collection<? extends Serializable> idList, String... indexNames);

    /**
     * 根据 entity 条件，删除记录
     *
     * @param wrapper 条件
     * @return 总成功条数
     */
    Integer delete(Wrapper<T> wrapper);

    /**
     * 根据 ID 更新
     *
     * @param entity 更新对象
     * @return 总成功条数
     */
    Integer updateById(T entity);

    /**
     * 根据 ID 更新 可指定路由
     *
     * @param routing 路由
     * @param entity  更新对象
     * @return 总成功条数
     */
    Integer updateById(String routing, T entity);

    /**
     * 根据 ID 更新 可指定多索引
     *
     * @param entity     更新对象
     * @param indexNames 指定更新的索引名称数组
     * @return 总成功条数
     */
    Integer updateById(T entity, String... indexNames);

    /**
     * 根据 ID 更新 可指定路由和多索引
     *
     * @param routing    路由
     * @param entity     更新对象
     * @param indexNames 指定更新的索引名称数组
     * @return 总成功条数
     */
    Integer updateById(String routing, T entity, String... indexNames);

    /**
     * 根据ID 批量更新
     *
     * @param entityList 更新对象列表
     * @return 总成功条数
     */
    Integer updateBatchByIds(Collection<T> entityList);

    /**
     * 根据ID 批量更新 可指定路由
     *
     * @param routing    路由
     * @param entityList 更新对象列表
     * @return 总成功条数
     */
    Integer updateBatchByIds(String routing, Collection<T> entityList);

    /**
     * 根据ID 批量更新 可指定多索引
     *
     * @param entityList 更新对象列表
     * @param indexNames 指定更新的索引名称数组
     * @return 总成功条数
     */
    Integer updateBatchByIds(Collection<T> entityList, String... indexNames);

    /**
     * 根据ID 批量更新 可指定路由及多索引
     *
     * @param routing    路由
     * @param entityList 更新对象列表
     * @param indexNames 指定更新的索引名称数组
     * @return 总成功条数
     */
    Integer updateBatchByIds(String routing, Collection<T> entityList, String... indexNames);

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        更新对象
     * @param updateWrapper 条件
     * @return 成功条数
     */
    Integer update(T entity, Wrapper<T> updateWrapper);

    /**
     * 根据 ID 查询
     *
     * @param id 主键
     * @return 指定的返回对象
     */
    T selectById(Serializable id);

    /**
     * 根据 ID 查询 可指定路由
     *
     * @param routing 路由
     * @param id      主键
     * @return 指定的返回对象
     */
    T selectById(String routing, Serializable id);

    /**
     * 根据 ID 查询 可指定多索引
     *
     * @param id         主键
     * @param indexNames 指定查询的索引名数组
     * @return 指定的返回对象
     */
    T selectById(Serializable id, String... indexNames);

    /**
     * 根据 ID 查询 可指定路由及多索引
     *
     * @param routing    路由
     * @param id         主键
     * @param indexNames 指定查询的索引名数组
     * @return 指定的返回对象
     */
    T selectById(String routing, Serializable id, String... indexNames);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键列表
     * @return 指定的返回对象列表
     */
    List<T> selectBatchIds(Collection<? extends Serializable> idList);

    /**
     * 查询（根据ID 批量查询） 可指定路由
     *
     * @param routing 路由
     * @param idList  主键列表
     * @return 指定的返回对象列表
     */
    List<T> selectBatchIds(String routing, Collection<? extends Serializable> idList);

    /**
     * 查询（根据ID 批量查询） 可指定多索引
     *
     * @param idList     主键列表
     * @param indexNames 指定查询的索引名数组
     * @return 指定的返回对象列表
     */
    List<T> selectBatchIds(Collection<? extends Serializable> idList, String... indexNames);

    /**
     * 查询（根据ID 批量查询） 可指定路由及多索引
     *
     * @param routing    路由
     * @param idList     主键列表
     * @param indexNames 指定查询的索引名数组
     * @return 指定的返回对象列表
     */
    List<T> selectBatchIds(String routing, Collection<? extends Serializable> idList, String... indexNames);

    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param wrapper 条件
     * @return 指定的返回对象
     */
    T selectOne(Wrapper<T> wrapper);

    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param wrapper 条件
     * @return 指定的返回对象列表
     */
    List<T> selectList(Wrapper<T> wrapper);

    /**
     * 设置当前Mapper默认激活的全局索引名称 务必谨慎操作,设置后全局生效,永驻jvm,除非项目重启
     *
     * @param indexName 索引名称
     * @return 是否成功
     */
    Boolean setCurrentActiveIndex(String indexName);

    /**
     * 设置当前Mapper默认的RequestOptions,不设置则使用默认配置,务必谨慎操作,设置后全局生效,永驻jvm,除非项目重启
     *
     * @param requestOptions 请求配置
     * @return 是否成功
     */
    Boolean setRequestOptions(RequestOptions requestOptions);
}
