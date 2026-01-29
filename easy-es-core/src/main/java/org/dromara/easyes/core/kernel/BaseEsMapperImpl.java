package org.dromara.easyes.core.kernel;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.AcknowledgedResponse;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.NestedIdentity;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;
import co.elastic.clients.elasticsearch.sql.QueryResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.http.util.EntityUtils;
import org.dromara.easyes.annotation.rely.IdType;
import org.dromara.easyes.annotation.rely.RefreshPolicy;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.enums.EsQueryTypeEnum;
import org.dromara.easyes.common.enums.OrderTypeEnum;
import org.dromara.easyes.common.join.BaseJoin;
import org.dromara.easyes.common.utils.*;
import org.dromara.easyes.common.utils.jackson.JsonUtils;
import org.dromara.easyes.core.biz.*;
import org.dromara.easyes.core.cache.BaseCache;
import org.dromara.easyes.core.cache.GlobalConfigCache;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.IndexUtils;
import org.dromara.easyes.core.toolkit.PageHelper;
import org.dromara.easyes.core.toolkit.PrintUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * 核心 所有支持方法接口实现类
 * <p>
 * 内部实现:
 * 核心网络请求类：{@link ElasticsearchClient}、
 * 动态封装request类：{@link WrapperProcessor}、
 * 查询类型枚举：{@link EsQueryTypeEnum}、
 * </p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Setter
public class BaseEsMapperImpl<T> implements BaseEsMapper<T> {
    /**
     * restHighLevel client
     */
    private ElasticsearchClient client;
    /**
     * T 对应的类
     */
    private Class<T> entityClass;

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public Boolean existsIndex(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            throw ExceptionUtils.eee("indexName can not be empty");
        }
        return IndexUtils.existsIndex(client, indexName);
    }

    @Override
    public GetIndexResponse getIndex() {
        return IndexUtils.getIndex(client, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public GetIndexResponse getIndex(String indexName) {
        return IndexUtils.getIndex(client, WrapperProcessor.getIndexName(entityClass, indexName));
    }

    @Override
    public Boolean createIndex() {
        return createIndex(EntityInfoHelper.getIndexName(entityClass));
    }

    @Override
    public Boolean createIndex(String indexName) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo, entityClass);
        if (StringUtils.isNotBlank(indexName)) {
            createIndexParam.setIndexName(indexName);
        }
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

    @Override
    public Boolean createIndex(Wrapper<T> wrapper) {
        wrapper.indexNames.forEach(indexName -> doCreateIndex(wrapper, indexName));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateIndex(Wrapper<T> wrapper) {
        wrapper.indexNames.forEach(indexName -> doUpdateIndex(wrapper, indexName));
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteIndex(String... indexNames) {
        Assert.notEmpty(indexNames, "indexNames can not be empty");
        Arrays.stream(indexNames)
                .forEach(indexName -> {
                    boolean success = IndexUtils.deleteIndex(client, indexName);
                    Assert.isTrue(success, String.format("delete index: %s failed,", indexName));
                });
        return Boolean.TRUE;
    }

    @Override
    public Integer refresh() {
        return this.refresh(EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer refresh(String... indexNames) {
        RefreshRequest request = RefreshRequest.of(x -> x.index(Arrays.asList(indexNames)));
        try {
            PrintUtils.printDsl(request, client);
            RefreshResponse refresh = client.indices().refresh(request);
            if (refresh.shards() != null) {
                return refresh.shards().successful().intValue();
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            throw ExceptionUtils.eee("refresh index exception e", e);
        }
    }

    @Override
    @SneakyThrows
    public String executeSQL(String sql) {
        // 如果是下划线转驼峰，ee帮助处理
        if (GlobalConfigCache.getGlobalConfig().getDbConfig().isMapUnderscoreToCamelCase()) {
            sql = StringUtils.camelToUnderline(sql);
        }
        PrintUtils.printSql(sql);
        String finalSql = sql;
        QueryResponse response = client.sql().query(x -> x.query(finalSql).format("json"));
        return JsonpUtils.toString(response, new StringBuilder()).toString();
    }

    /**
     * 执行静态dsl语句 可指定作用的索引
     *
     * @param method   方法名
     * @param endpoint 端点
     * @param dsl      dsl语句
     * @return 执行结果 jsonString
     */
    @Override
    @SneakyThrows
    public String executeDSL(String method, String endpoint, String dsl) {
        Assert.notNull(endpoint, "endpoint must not null");
        Request request = new Request(method, endpoint);
        request.setJsonEntity(dsl);
        PrintUtils.printDsl(method, endpoint, dsl);
        Response response = ((RestClientTransport) client._transport()).restClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public SearchResponse<T> search(Wrapper<T> wrapper) {
        // 执行普通混合查询, 不含searchAfter分页
        return getSearchResponse(wrapper, null, false);
    }

    @Override
    public ScrollResponse<T> scroll(ScrollRequest searchScrollRequest, TransportOptions requestOptions) throws IOException {
        PrintUtils.printDsl(searchScrollRequest, client);
        ScrollResponse<T> response = client.withTransportOptions(requestOptions).scroll(searchScrollRequest, entityClass);
        printResponseErrors(response);
        return response;
    }

    @Override
    public SearchRequest.Builder getSearchBuilder(Wrapper<T> wrapper) {
        return WrapperProcessor.buildSearchBuilder(wrapper, entityClass);
    }

    @Override
    public String getSource(Wrapper<T> wrapper) {
        try {
            // 用户在wrapper中指定的混合查询条件优先级最高
            SearchRequest.Builder builder = Optional.ofNullable(wrapper.searchBuilder)
                    .orElse(WrapperProcessor.buildSearchBuilder(wrapper, entityClass))
                    .index(WrapperProcessor.getIndexName(entityClass, wrapper.indexNames))
                    .routing(wrapper.routing)
                    .preference(wrapper.preference);
            return builder.build().toString();
        } catch (Exception e) {
            throw ExceptionUtils.eee("get search source exception", e);
        }
    }

    @Override
    public EsPageInfo<T> pageQuery(Wrapper<T> wrapper, Integer pageNum, Integer pageSize) {
        // 兼容分页参数
        pageNum = pageNum == null || pageNum <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_NUM : pageNum;
        pageSize = pageSize == null || pageSize <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;

        wrapper.from = (pageNum - 1) * pageSize;
        wrapper.size = pageSize;

        // 请求es获取数据
        SearchResponse<T> response = getSearchResponse(wrapper);

        // 解析数据
        List<Hit<T>> searchHits = parseSearchHitArray(response);
        List<T> dataList = searchHits.stream()
                .map(searchHit -> parseOne(searchHit, wrapper))
                .collect(Collectors.toList());
        long count = parseCount(response, Objects.nonNull(wrapper.distinctField));
        return PageHelper.getPageInfo(dataList, count, pageNum, pageSize);
    }

    @Override
    public SAPageInfo<T> searchAfterPage(Wrapper<T> wrapper, List<FieldValue> searchAfter, Integer pageSize) {
        // searchAfter语法规定 或from只允许为0或-1或不传,否则es会报错, 推荐不指定, 直接传null即可
        boolean illegalArg = Objects.nonNull(wrapper.from) && (!wrapper.from.equals(ZERO) || !wrapper.from.equals(MINUS_ONE));
        if (illegalArg) {
            throw ExceptionUtils.eee("The wrapper.from in searchAfter must be 0 or -1 or null, null is recommended");
        }

        // 兼容分页参数
        pageSize = pageSize == null || pageSize <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;
        wrapper.size = pageSize;

        // 请求es获取数据
        SearchResponse<T> response =
                CollectionUtils.isEmpty(searchAfter) ? getSearchResponse(wrapper) : getSearchResponse(wrapper, searchAfter, true);

        // 解析数据
        List<Hit<T>> searchHits = parseSearchHitArray(response);
        List<T> dataList = searchHits.stream().map(searchHit -> parseOne(searchHit, wrapper))
                .collect(Collectors.toList());
        List<FieldValue> nextSearchAfter = searchHits.stream()
                .map(Hit::sort)
                .reduce((first, second) -> second)
                .orElse(null);
        long count = parseCount(response, Objects.nonNull(wrapper.distinctField));
        return PageHelper.getSAPageInfo(dataList, count, searchAfter, nextSearchAfter, pageSize);
    }

    @Override
    public Long selectCount(Wrapper<T> wrapper) {
        return selectCount(wrapper, Objects.nonNull(wrapper.distinctField));
    }

    @Override
    public SearchResponse<T> search(SearchRequest searchRequest, TransportOptions requestOptions) throws IOException {
        PrintUtils.printDsl(searchRequest, client);
        SearchResponse<T> response = client.withTransportOptions(requestOptions).search(searchRequest, entityClass);
        printResponseErrors(response);
        return response;
    }

    /**
     * 非search查询获取boolQuery
     *
     * @param wrapper wrapper
     * @return boolQuery
     */
    private BoolQuery getBoolQuery(Wrapper<T> wrapper) {
        return Optional.ofNullable(wrapper.searchBuilder)
                .flatMap(x -> Optional.ofNullable(x.build().query()))
                .map(Query::bool)
                .orElse(WrapperProcessor.initBoolQueryBuilder(wrapper.paramQueue, entityClass).build());
    }

    @Override
    public Integer insert(T entity) {
        return insert(null, null, entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insert(String routing, T entity) {
        return insert(routing, null, entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insert(String routing, String parentId, T entity) {
        return insert(routing, parentId, entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insert(T entity, String... indexNames) {
        return insert(null, null, entity, indexNames);
    }

    @Override
    public Integer insert(String routing, T entity, String... indexNames) {
        return insert(routing, null, entity, indexNames);
    }

    @Override
    public Integer insert(String routing, String parentId, T entity, String... indexNames) {
        Assert.notNull(entity, "insert entity must not be null");

        // 执行插入
        return WrapperProcessor.getIndexName(entityClass, indexNames).stream()
                .mapToInt(indexName -> doInsert(entity, routing, parentId, indexName))
                .sum();
    }

    @Override
    public Integer insertBatch(Collection<T> entityList) {
        return insertBatch(null, null, entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insertBatch(String routing, Collection<T> entityList) {
        return insertBatch(routing, entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insertBatch(String routing, String parentId, Collection<T> entityList) {
        return insertBatch(routing, parentId, entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insertBatch(Collection<T> entityList, String... indexNames) {
        return insertBatch(null, null, entityList, indexNames);
    }

    @Override
    public Integer insertBatch(String routing, Collection<T> entityList, String... indexNames) {
        return insertBatch(routing, null, entityList, indexNames);
    }

    @Override
    public Integer insertBatch(String routing, String parentId, Collection<T> entityList, String... indexNames) {
        // 老汉裤子都脱了 你告诉我没有数据 怎么*入?
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }

        // 在每条指定的索引上批量执行数据插入
        return WrapperProcessor.getIndexName(entityClass, indexNames).stream()
                .mapToInt(indexName -> doInsertBatch(entityList, routing, parentId, indexName))
                .sum();
    }

    @Override
    public Integer deleteById(Serializable id) {
        return deleteById(null, id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteById(String routing, Serializable id) {
        return deleteById(routing, id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteById(Serializable id, String... indexNames) {
        return deleteById(null, id, indexNames);
    }

    @Override
    public Integer deleteById(String routing, Serializable id, String... indexNames) {
        return WrapperProcessor.getIndexName(entityClass, indexNames).stream()
                .mapToInt(indexName -> doDeleteById(id, routing, indexName))
                .sum();
    }

    @Override
    public Integer deleteBatchIds(Collection<? extends Serializable> idList) {
        return deleteBatchIds(null, idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteBatchIds(String routing, Collection<? extends Serializable> idList) {
        return deleteBatchIds(routing, idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteBatchIds(Collection<? extends Serializable> idList, String... indexNames) {
        return deleteBatchIds(null, idList, indexNames);
    }

    @Override
    public Integer deleteBatchIds(String routing, Collection<? extends Serializable> idList, String... indexNames) {
        Assert.notEmpty(idList, "the collection of id must not empty");
        return WrapperProcessor.getIndexName(entityClass, indexNames).stream()
                .mapToInt(indexName -> doDeleteBatchIds(idList, routing, indexName))
                .sum();
    }

    @Override
    public Long selectCount(Wrapper<T> wrapper, boolean distinct) {
        if (distinct) {
            // 去重, 总数来源于桶, 只查id列,节省内存 拷贝是防止追加的只查id列影响到count后的其它查询
            Wrapper<T> clone = wrapper.clone();
            clone.include = new String[]{EntityInfoHelper.getEntityInfo(entityClass).getKeyProperty()};
            SearchResponse<T> response = getSearchResponse(clone);
            return parseCount(response, Objects.nonNull(clone.distinctField));
        } else {
            // 不去重,直接count获取,效率更高
            BoolQuery query = getBoolQuery(wrapper);

            CountRequest req = CountRequest.of(a -> a
                    .index(WrapperProcessor.getIndexName(entityClass, wrapper.indexNames))
                    .routing(wrapper.routing)
                    .preference(wrapper.preference)
                    .query(query._toQuery())
            );
            CountResponse rsp;
            try {
                PrintUtils.printDsl(req, client);
                rsp = client.withTransportOptions(getTransportOptions()).count(req);
            } catch (IOException e) {
                throw ExceptionUtils.eee("selectCount exception", e);
            }
            return rsp.count();
        }
    }

    @Override
    public Integer updateById(T entity) {
        return updateById(null, entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateById(String routing, T entity) {
        return updateById(routing, entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateById(T entity, String... indexNames) {
        return updateById(null, entity, indexNames);
    }

    @Override
    public Integer updateById(String routing, T entity, String... indexNames) {
        Assert.notNull(entity, "entity must not be null");

        // 获取id值
        String idValue = getIdValue(entity);

        // 在每条索引上执行更新
        return WrapperProcessor.getIndexName(entityClass, indexNames).stream()
                .mapToInt(indexName -> doUpdateById(entity, idValue, routing, indexName))
                .sum();
    }

    @Override
    public Integer updateBatchByIds(Collection<T> entityList) {
        return updateBatchByIds(null, entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateBatchByIds(String routing, Collection<T> entityList) {
        return updateBatchByIds(routing, entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateBatchByIds(Collection<T> entityList, String... indexNames) {
        return updateBatchByIds(null, entityList, indexNames);
    }

    @Override
    public Integer updateBatchByIds(String routing, Collection<T> entityList, String... indexNames) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }

        // 在每条指定索引上批量执行更新
        return WrapperProcessor.getIndexName(entityClass, indexNames).stream()
                .mapToInt(indexName -> doUpdateBatchByIds(entityList, routing, indexName))
                .sum();
    }

    @Override
    public Integer update(T entity, Wrapper<T> updateWrapper) {
        if (Objects.isNull(entity) && CollectionUtils.isEmpty(updateWrapper.updateParamList)) {
            return BaseEsConstants.ZERO;
        }

        // 在每条指定索引上执行更新操作
        return WrapperProcessor.getIndexName(entityClass, updateWrapper.indexNames).stream()
                .mapToInt(indexName -> doUpdate(entity, updateWrapper, indexName))
                .sum();
    }

    @Override
    public T selectById(Serializable id) {
        return selectById(null, id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public T selectById(String routing, Serializable id) {
        return selectById(routing, id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public T selectById(Serializable id, String... indexNames) {
        return selectById(null, id, indexNames);
    }

    @Override
    public T selectById(String routing, Serializable id, String... indexNames) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }

        // 从指定的多条索引上去获取, 返回最先命中的数据
        return WrapperProcessor.getIndexName(entityClass, indexNames).stream()
                .map(indexName -> doSelectById(id, routing, indexName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList) {
        return selectBatchIds(null, idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public List<T> selectBatchIds(String routing, Collection<? extends Serializable> idList) {
        return selectBatchIds(routing, idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList, String... indexNames) {
        return selectBatchIds(null, idList, indexNames);
    }

    @Override
    public List<T> selectBatchIds(String routing, Collection<? extends Serializable> idList, String... indexNames) {
        Assert.notEmpty(idList, "id collection must not be null or empty");

        // 在每条指定索引上执行查询
        List<T> result = new ArrayList<>();
        WrapperProcessor.getIndexName(entityClass, indexNames)
                .forEach(indexName -> result.addAll(doSelectBatchIds(idList, routing, indexName)));
        return result;
    }

    @Override
    public T selectOne(Wrapper<T> wrapper) {
        // 请求es获取数据
        SearchResponse<T> searchResponse = getSearchResponse(wrapper);
        long count = parseCount(searchResponse, Objects.nonNull(wrapper.distinctField));
        boolean invalid = (count > ONE && (Objects.nonNull(wrapper.size) && wrapper.size > ONE))
                          || (count > ONE && Objects.isNull(wrapper.size));
        if (invalid) {
            LogUtils.error("found more than one result:" + count, "please use wrapper.limit to limit 1");
            throw ExceptionUtils.eee("found more than one result: %d, please use wrapper.limit to limit 1", count);
        }

        // 解析数据
        List<Hit<T>> searchHits = parseSearchHitArray(searchResponse);
        if (CollectionUtils.isEmpty(searchHits)) {
            return null;
        }
        return parseOne(searchHits.get(0), wrapper);
    }

    @Override
    public List<T> selectList(Wrapper<T> wrapper) {
        // 请求es获取数据
        List<Hit<T>> searchHits = getSearchHits(wrapper);
        if (CollectionUtils.isEmpty(searchHits)) {
            return Collections.emptyList();
        }

        // 批量解析
        return searchHits.stream()
                .map(searchHit -> parseOne(searchHit, wrapper))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean setCurrentActiveIndex(String indexName) {
        synchronized (this) {
            EntityInfoHelper.getEntityInfo(entityClass).setIndexName(indexName);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean setRequestOptions(TransportOptions requestOptions) {
        synchronized (this) {
            EntityInfoHelper.getEntityInfo(entityClass).setRequestOptions(requestOptions);
        }
        return Boolean.TRUE;
    }

    /**
     * 执行创建索引
     *
     * @param wrapper   条件
     * @param indexName 索引名
     */
    private void doCreateIndex(Wrapper<T> wrapper, String indexName) {
        CreateIndexParam createIndexParam = new CreateIndexParam();

        // 设置索引名
        createIndexParam.setIndexName(indexName);

        // 设置用户自定义的settings
        Optional.ofNullable(wrapper.settings).ifPresent(createIndexParam::setSettings);

        // 通过wrapper指定的mapping参数封装
        List<EsIndexParam> indexParamList = wrapper.esIndexParamList;
        createIndexParam.setEsIndexParamList(indexParamList);

        // 设置用户自定义的mapping信息
        Optional.ofNullable(wrapper.mapping).ifPresent(createIndexParam::setMapping);

        // 设置别名
        Optional.ofNullable(wrapper.aliasName).ifPresent(createIndexParam::setAliasName);

        // 创建索引
        boolean success = IndexUtils.createIndex(client, EntityInfoHelper.getEntityInfo(entityClass), createIndexParam);
        Assert.isTrue(success, String.format("create index:%s failed", indexName));
    }

    /**
     * 执行更新索引
     *
     * @param wrapper   条件
     * @param indexName 索引名
     */
    private void doUpdateIndex(Wrapper<T> wrapper, String indexName) {
        // 判断指定索引是否存在
        boolean existsIndex = this.existsIndex(indexName);
        Assert.isTrue(existsIndex, String.format("update index: %s failed, because of this index not exists", indexName));

        if (Objects.isNull(wrapper.mapping)) {
            Assert.notEmpty(wrapper.esIndexParamList, String.format("update index: %s failed, because of empty update args", indexName));
        }

        TypeMapping.Builder mapping = Objects.nonNull(wrapper.mapping) ? wrapper.mapping :
                IndexUtils.initMapping(EntityInfoHelper.getEntityInfo(entityClass), wrapper.esIndexParamList);
        TypeMapping build = mapping.build();

        // 更新mapping
        PutMappingRequest putMappingRequest = PutMappingRequest.of(a -> a
                .dateDetection(build.dateDetection())
                .dynamic(build.dynamic())
                .dynamicDateFormats(build.dynamicDateFormats())
                .dynamicTemplates(build.dynamicTemplates())
                .meta(build.meta())
                .numericDetection(build.numericDetection())
                .properties(build.properties())
                .routing(build.routing())
                .source(build.source())
                .runtime(build.runtime())
                .index(indexName)
        );

        try {
            PrintUtils.printDsl(putMappingRequest, client);
            AcknowledgedResponse acknowledgedResponse = client.withTransportOptions(getTransportOptions()).indices().putMapping(putMappingRequest);
            Assert.isTrue(acknowledgedResponse.acknowledged(), String.format("update index failed, index: %s", indexName));
        } catch (IOException e) {
            throw ExceptionUtils.eee("update index exception", e);
        }
    }

    @Override
    public Integer delete(Wrapper<T> wrapper) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);

        DeleteByQueryRequest request = DeleteByQueryRequest.of(a -> a
                .index(WrapperProcessor.getIndexName(entityClass, wrapper.indexNames))
                .query(getBoolQuery(wrapper)._toQuery())
                .routing(wrapper.routing)
                .refresh(Refresh.True.equals(getRefreshPolicy()))
                .scrollSize(entityInfo == null ? null : entityInfo.getMaxResultWindow().longValue())
        );

        PrintUtils.printDsl(request, client);
        try {
            DeleteByQueryResponse bulkResponse = client.deleteByQuery(request);
            // 单次删除通常不会超过21亿, 这里为了兼容老API设计,仍转为int 2.0版本将调整为long
            return bulkResponse.deleted() == null ? BaseEsConstants.ZERO : bulkResponse.deleted().intValue();
        } catch (IOException e) {
            throw ExceptionUtils.eee("delete error, dsl:%s", e);
        }
    }

    /**
     * 获取客户端
     * @return es 客户端
     */
    public ElasticsearchClient getClient() {
        return client;
    }

    /**
     * 执行批量插入数据
     *
     * @param entityList 数据列表
     * @param routing    路由
     * @param parentId   父id
     * @param indexName  索引名
     * @return 总成功条数
     */
    private Integer doInsertBatch(Collection<T> entityList, String routing, String parentId, String indexName) {
        List<BulkOperation> operations = new ArrayList<>();
        entityList.forEach(entity -> {
            IndexRequest<T> indexRequest = buildIndexRequest(entity, routing, parentId, indexName).build();
            BulkOperation operation = BulkOperation.of(b -> b
                    .index(i -> i
                            .id(indexRequest.id())
                            .ifPrimaryTerm(indexRequest.ifPrimaryTerm())
                            .ifSeqNo(indexRequest.ifSeqNo())
                            .index(indexRequest.index())
                            .pipeline(indexRequest.pipeline())
                            .requireAlias(indexRequest.requireAlias())
                            .routing(indexRequest.routing())
                            .version(indexRequest.version())
                            .versionType(indexRequest.versionType())
                            .document(indexRequest.document())
                    )
            );
            operations.add(operation);
        });

        // 构建批量请求参数
        BulkRequest bulkRequest = BulkRequest.of(a -> a
                .routing(routing)
                .refresh(getRefreshPolicy())
                .operations(operations)
        );

        // 执行批量请求并返回结果
        return doBulkRequest(bulkRequest, Result.Created.jsonValue());
    }

    /**
     * 执行插入单条数据
     *
     * @param entity    插入对象
     * @param routing   路由
     * @param parentId  父id
     * @param indexName 索引名
     * @return 成功条数
     */
    private Integer doInsert(T entity, String routing, String parentId, String indexName) {
        // 构建请求入参
        IndexRequest<T> indexRequest = buildIndexRequest(entity, routing, parentId, indexName)
                .refresh(getRefreshPolicy()).build();

        PrintUtils.printDsl(indexRequest, client);
        try {
            IndexResponse indexResponse = client.withTransportOptions(getTransportOptions()).index(indexRequest);
            if (Objects.equals(indexResponse.result(), Result.Created)) {
                setId(entity, indexResponse.id());
                return BaseEsConstants.ONE;
            } else if (Objects.equals(indexResponse.result(), Result.Updated)) {
                // 该id已存在,数据被更新的情况
                return BaseEsConstants.ZERO;
            } else {
                throw ExceptionUtils.eee("insert failed, result:%s entity:%s", indexResponse.result(), entity);
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("insert entity:%s exception", e, entity.toString());
        }
    }

    /**
     * 执行根据id删除指定数据
     *
     * @param id        id
     * @param routing   路由
     * @param indexName 索引名
     * @return 成功条数
     */
    private Integer doDeleteById(Serializable id, String routing, String indexName) {
        DeleteRequest request = generateDelRequest(id, indexName)
                .routing(routing)
                .refresh(getRefreshPolicy())
                .build();

        PrintUtils.printDsl(request, client);
        try {
            DeleteResponse deleteResponse = client.withTransportOptions(getTransportOptions()).delete(request);
            if (Objects.equals(deleteResponse.result(), Result.Deleted)) {
                return BaseEsConstants.ONE;
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("deleteById exception, id:%s", e, id.toString());
        }
        return BaseEsConstants.ZERO;
    }

    /**
     * 执行根据id批量删除
     *
     * @param idList    id列表
     * @param routing   路由
     * @param indexName 索引名
     * @return 总成功条数
     */
    private Integer doDeleteBatchIds(Collection<? extends Serializable> idList, String routing, String indexName) {
        List<BulkOperation> operations = new ArrayList<>();
        idList.forEach(id -> {
            DeleteRequest deleteRequest = generateDelRequest(id, indexName).build();
            BulkOperation operation = BulkOperation.of(a ->
                    a.delete(b -> b
                            .id(deleteRequest.id())
                            .ifPrimaryTerm(deleteRequest.ifPrimaryTerm())
                            .ifSeqNo(deleteRequest.ifSeqNo())
                            .index(deleteRequest.index())
                            .routing(deleteRequest.routing())
                            .version(deleteRequest.version())
                            .versionType(deleteRequest.versionType())
                    ));
            operations.add(operation);
        });

        BulkRequest request = BulkRequest.of(a -> a
                .routing(routing)
                .refresh(getRefreshPolicy())
                .operations(operations)
        );

        return doBulkRequest(request, Result.Deleted.jsonValue());
    }

    /**
     * 执行根据id更新
     *
     * @param entity    更新数据
     * @param idValue   id值
     * @param routing   路由
     * @param indexName 索引名
     * @return 更新条数
     */
    private Integer doUpdateById(T entity, String idValue, String routing, String indexName) {
        // 构建更新请求参数
        UpdateRequest<T, T> updateRequest = buildUpdateRequest(entity, idValue, indexName)
                .routing(routing)
                .refresh(getRefreshPolicy())
                .build();

        PrintUtils.printDsl(updateRequest, client);

        // 执行更新
        try {
            UpdateResponse<T> updateResponse = client.withTransportOptions(getTransportOptions()).update(updateRequest, entityClass);
            if (Objects.equals(updateResponse.result(), Result.Updated)) {
                return BaseEsConstants.ONE;
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("updateById exception,entity:%s", e, entity.toString());
        }

        return BaseEsConstants.ZERO;
    }

    /**
     * 执行根据条件更新
     *
     * @param entity        数据
     * @param updateWrapper 条件
     * @param indexName     索引名
     * @return 总成功条数
     */
    private Integer doUpdate(T entity, Wrapper<T> updateWrapper, String indexName) {
        // 查询数据列表
        List<T> list = selectListByUpdateWrapper(updateWrapper, indexName);
        if (CollectionUtils.isEmpty(list)) {
            return BaseEsConstants.ZERO;
        }

        // 获取更新文档内容
        T update = entity == null ? buildUpdateDoc(updateWrapper) : entity;

        List<BulkOperation> operations = new ArrayList<>();
        for (T item : list) {
            BulkOperation operation = BulkOperation.of(a -> a.update(b -> b
                    .index(indexName)
                    .routing(updateWrapper.routing)
                    .id(getId(item))
                    .action(c -> c.doc(update))
            ));
            operations.add(operation);
        }

        // 批量更新
        BulkRequest bulkRequest = BulkRequest.of(a -> a
                .routing(updateWrapper.routing)
                .refresh(getRefreshPolicy())
                .operations(operations)
        );
        return doBulkRequest(bulkRequest, Result.Updated.jsonValue());
    }

    /**
     * 执行根据id批量更新
     *
     * @param entityList 更新数据列表
     * @param routing    路由
     * @param indexName  索引名
     * @return 总成功条数
     */
    private Integer doUpdateBatchByIds(Collection<T> entityList, String routing, String indexName) {
        // 封装批量请求参数
        List<BulkOperation> operations = new ArrayList<>();
        entityList.forEach(e -> {
            String idValue = getIdValue(e);
            UpdateRequest<T, T> updateRequest = buildUpdateRequest(e, idValue, indexName).build();
            BulkOperation operation = BulkOperation.of(a ->
                    a.update(b -> b
                            .id(updateRequest.id())
                            .ifPrimaryTerm(updateRequest.ifPrimaryTerm())
                            .ifSeqNo(updateRequest.ifSeqNo())
                            .index(updateRequest.index())
                            .requireAlias(updateRequest.requireAlias())
                            .retryOnConflict(updateRequest.retryOnConflict())
                            .routing(updateRequest.routing())
                    ));
            operations.add(operation);
        });

        BulkRequest request = BulkRequest.of(a -> a
                .routing(routing)
                .refresh(getRefreshPolicy())
                .operations(operations)
        );

        return doBulkRequest(request, Result.Updated.jsonValue());
    }

    /**
     * 执行根据id批量查询
     *
     * @param idList    id数组
     * @param routing   路由
     * @param indexName 索引名
     * @return 数据
     */
    private List<T> doSelectBatchIds(Collection<? extends Serializable> idList, String routing, String indexName) {
        // 构造查询参数
        List<String> ids = idList.stream().map(Object::toString).collect(Collectors.toList());
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(WrapperProcessor.getIndexName(entityClass, indexName))
                .routing(routing)
                .query(QueryBuilders.ids().values(ids).build()._toQuery())
                .size(idList.size())
                .build();

        // 请求es获取数据
        List<Hit<T>> searchHits = getSearchHits(searchRequest);
        if (CollectionUtils.isEmpty(searchHits)) {
            return Collections.emptyList();
        }

        // 批量解析数据
        return searchHits.stream()
                .map(this::parseOne)
                .collect(Collectors.toList());
    }

    /**
     * 执行根据id查询
     *
     * @param id        id
     * @param routing   路由
     * @param indexName 索引名
     * @return 数据
     */
    private T doSelectById(Serializable id, String routing, String indexName) {
        // 构造查询参数
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .routing(routing)
                .query(QueryBuilders.ids().values(id.toString()).build()._toQuery())
                .build();
        // 请求es获取数据
        List<Hit<T>> searchHits = getSearchHits(searchRequest);
        if (CollectionUtils.isEmpty(searchHits)) {
            return null;
        }

        // 解析数据并返回
        return parseOne(searchHits.get(0));
    }


    /**
     * 生成DelRequest请求参数
     *
     * @param id        id
     * @param indexName 索引名
     * @return DelRequest
     */
    private DeleteRequest.Builder generateDelRequest(Serializable id, String indexName) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }
        return new DeleteRequest.Builder()
                .id(id.toString())
                .index(indexName);
    }

    /**
     * 获取es查询结果返回体
     *
     * @param wrapper         条件
     * @param searchAfter     searchAfter参数
     * @param needSearchAfter 是否需要searchAfter
     * @return es返回体
     */
    private SearchResponse<T> getSearchResponse(Wrapper<T> wrapper, List<FieldValue> searchAfter, boolean needSearchAfter) {
        // 用户在wrapper中指定的混合查询条件优先级最高
        SearchRequest.Builder builder = Optional.ofNullable(wrapper.searchBuilder)
                .orElse(WrapperProcessor.buildSearchBuilder(wrapper, entityClass))
                .index(WrapperProcessor.getIndexName(entityClass, wrapper.indexNames))
                .routing(wrapper.routing)
                .preference(wrapper.preference);

        if (needSearchAfter && CollectionUtils.isNotEmpty(searchAfter)) {
            builder.searchAfter(searchAfter);
        }

        SearchRequest searchRequest = builder.build();

        // searchAfter必须要进行排序，不排序无法进行分页
        if (needSearchAfter && searchRequest.sort().isEmpty()) {
            throw ExceptionUtils.eee("searchAfter必须要进行排序");
        }
        PrintUtils.printDsl(searchRequest, client);

        try {
            // 执行查询
            SearchResponse<T> response = client.withTransportOptions(getTransportOptions()).search(searchRequest, entityClass);
            printResponseErrors(response);
            return response;
        } catch (Exception e) {
            throw ExceptionUtils.eee("search exception", e);
        }
    }

    /**
     * 查询数据列表
     *
     * @param wrapper   查询参数
     * @param indexName 索引名
     * @return 数据列表
     */
    private List<T> selectListByUpdateWrapper(Wrapper<T> wrapper, String indexName) {
        // 构建查询条件
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(indexName)
                .routing(wrapper.routing)
                .preference(wrapper.preference);

        if (!Objects.isNull(wrapper.searchBuilder)) {
            builder.query(wrapper.searchBuilder.build().query());
        } else {
            EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
            if (entityInfo == null) {
                throw ExceptionUtils.eee("entityClass must not be null or empty");
            }
            Integer batchUpdateThreshold = GlobalConfigCache.getGlobalConfig().getDbConfig().getBatchUpdateThreshold();
            int maxResultWindow = entityInfo.getMaxResultWindow();
            int size = batchUpdateThreshold > maxResultWindow ? maxResultWindow : batchUpdateThreshold;
            builder
                    // 只查id列,节省内存
                    .source(a -> a.filter(b -> b.includes(entityInfo.getKeyProperty())))
                    .trackTotalHits(a -> a.enabled(true))
                    .query(WrapperProcessor.initBoolQueryBuilder(wrapper.paramQueue, entityClass).build()._toQuery())
                    .size(size)
            ;
        }

        SearchRequest searchRequest = builder.build();
        PrintUtils.printDsl(searchRequest, client);
        try {
            // 查询数据明细
            SearchResponse<T> response = client.withTransportOptions(getTransportOptions()).search(searchRequest, entityClass);
            printResponseErrors(response);
            List<Hit<T>> searchHits = parseSearchHitArray(response);
            return searchHits.stream()
                    .map(this::parseOne)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw ExceptionUtils.eee("selectIdList exception", e);
        }
    }

    /**
     * 构建更新数据请求参数
     *
     * @param entity    实体
     * @param idValue   id值
     * @param indexName 索引名
     * @return 更新请求参数
     */
    private UpdateRequest.Builder<T, T> buildUpdateRequest(T entity, String idValue, String indexName) {
        return new UpdateRequest.Builder<T, T>()
                .id(idValue)
                .index(indexName)
                .doc(entity);
    }

    /**
     * 解析获取据数总数
     *
     * @param response es返回的数据
     * @param distinct 是否去重统计
     * @return 总数
     */
    private long parseCount(SearchResponse<T> response, boolean distinct) {
        AtomicLong repeatNum = new AtomicLong(0);
        if (distinct) {
            Optional.ofNullable(response.aggregations())
                    .ifPresent(aggregations -> {
                        Aggregate parsedCardinality = aggregations.get(REPEAT_NUM_KEY);
                        Optional.ofNullable(parsedCardinality).ifPresent(p -> repeatNum.getAndAdd(p.cardinality().value()));
                    });
        } else {
            Optional.ofNullable(response.hits())
                    .flatMap(searchHits -> Optional.ofNullable(searchHits.total()))
                    .ifPresent(totalHits -> repeatNum.getAndAdd(totalHits.value()));
        }
        return repeatNum.get();
    }

    /**
     * 从searchHit中解析一条数据
     *
     * @param searchHit es返回数据
     * @return 实际想要的数据
     */
    private T parseOne(Hit<T> searchHit) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        T entity = searchHit.source();

        // id字段处理
        setId(entity, searchHit.id());

        // 得分字段处理
        setScore(entity, searchHit.score(), entityInfo);

        // 距离字段处理
        setDistance(entity, searchHit.sort(), entityInfo);

        return entity;
    }

    /**
     * 从searchHit中解析一条数据
     *
     * @param searchHit es返回数据
     * @param wrapper   参数包装类
     * @return 实际想要的数据
     */
    private T parseOne(Hit<T> searchHit, Wrapper<T> wrapper) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);

        // 解析json
        T entity = searchHit.source();

        // 主类中高亮字段处理
        Map<String, List<String>> highlightFields = searchHit.highlight();
        if (CollectionUtils.isNotEmpty(entityInfo.getHighlightParams()) && CollectionUtils.isNotEmpty(highlightFields)) {
            Map<String, String> highlightFieldMap = getHighlightFieldMap();
            highlightFields.forEach((key, value) -> {
                String highLightValue = String.join("", value);
                setHighlightValue(entity, highlightFieldMap.get(key), highLightValue);
            });
        }

        // 嵌套类中的高亮处理
        setInnerHighlight(searchHit, entity, entityInfo.getNestedOrObjectHighlightFieldMap());

        // 得分字段处理
        setScore(entity, searchHit.score(), entityInfo);

        // 距离字段处理
        setDistance(entity, searchHit.sort(), entityInfo, wrapper.baseSortParams);

        // id处理
        boolean includeId = WrapperProcessor.includeId(getRealIdFieldName(), wrapper);
        if (includeId) {
            setId(entity, searchHit.id());
        }

        return entity;
    }


    /**
     * 设置嵌套类型中的高亮
     *
     * @param searchHit               查询结果
     * @param root                    主实体对象
     * @param nestedHighlightFieldMap 字段缓存
     */
    private void setInnerHighlight(Hit<T> searchHit, T root, Map<Class<?>, Map<String, String>> nestedHighlightFieldMap) {
        // 遍历innerHits 批量设置
        if (CollectionUtils.isEmpty(searchHit.innerHits())) {
            return;
        }
        searchHit.innerHits()
                .forEach((k, v) -> {
                    List<Hit<JsonData>> hits = v.hits().hits();
                    hits.forEach(hit -> {
                        NestedIdentity nestedIdentity = hit.nested();
                        Map<String, List<String>> highlightFields = hit.highlight();
                        if (CollectionUtils.isNotEmpty(highlightFields) && nestedIdentity != null) {
                            highlightFields.forEach((k1, v1) -> {
                                String highLightContent = String.join("", v1);
                                NestedIdentity tmpNestedIdentity = nestedIdentity;
                                List<String> pathList = new ArrayList<>();
                                while (tmpNestedIdentity != null) {
                                    Optional.ofNullable(tmpNestedIdentity.field()).ifPresent(pathList::add);
                                    tmpNestedIdentity = tmpNestedIdentity.nested();
                                }
                                String highLightField = k1.replace(String.join(STR_SIGN, pathList) + STR_SIGN, EMPTY_STR);
                                processInnerHighlight(nestedIdentity.field(), root, nestedIdentity,
                                        highLightField, highLightContent, nestedHighlightFieldMap);
                            });
                        }
                    });
                });
    }

    /**
     * 递归处理嵌套类中的高亮
     *
     * @param path                    嵌套类路径
     * @param root                    根
     * @param nestedIdentity          嵌套路径
     * @param highlightField          高亮字段
     * @param highlightContent        高亮内容
     * @param nestedHighlightFieldMap 字段缓存
     */
    private void processInnerHighlight(String path, Object root, NestedIdentity nestedIdentity, String highlightField,
                                       String highlightContent, Map<Class<?>, Map<String, String>> nestedHighlightFieldMap) {
        // 反射, 获取嵌套对象
        Method method = BaseCache.getterMethod(root.getClass(), nestedIdentity.field());
        Object invoke = null;
        try {
            invoke = method.invoke(root);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtils.error("processInnerHighlight invoke error, class:%s,methodName:%s",
                    root.getClass().getSimpleName(), nestedIdentity.field());
        }

        // 嵌套对象为容器的情况
        if (invoke instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) invoke;
            Iterator<?> iterator = coll.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Object next = iterator.next();
                // 不在nestedIdentity中的项无需处理
                if (i == nestedIdentity.offset()) {
                    if (path.equals(nestedIdentity.field())) {
                        final NestedIdentity child = nestedIdentity.nested();
                        if (child != null) {
                            // 递归 对子项执行相同操作
                            processInnerHighlight(child.field(), next, child, highlightField, highlightContent, nestedHighlightFieldMap);
                        } else {
                            // 已找到需要被高亮的叶子节点
                            String realHighlightField = Optional.ofNullable(nestedHighlightFieldMap.get(next.getClass()))
                                    .map(highlightFieldMap -> highlightFieldMap.get(highlightField)).orElse(highlightField);
                            setHighlightValue(next, realHighlightField, highlightContent);
                        }
                    }
                }
                i++;
            }
        } else {
            // 不太可能发生, 因为非容器的嵌套类型无意义,可以直接大宽表,但考虑到健壮性,仍针对个别傻狍子用户兼容处理
            Object finalInvoke = invoke;
            Optional.ofNullable(finalInvoke).ifPresent(i -> {
                String realHighlightField = Optional.ofNullable(nestedHighlightFieldMap.get(finalInvoke.getClass()))
                        .map(highlightFieldMap -> highlightFieldMap.get(highlightField)).orElse(highlightField);
                setHighlightValue(i, realHighlightField, highlightContent);
            });
        }
    }

    private void setDistance(T entity, List<FieldValue> sortValues, EntityInfo entityInfo) {
        setDistance(entity, sortValues, entityInfo, null);
    }

    /**
     * 设置距离
     *
     * @param entity         实体对象
     * @param sortValues     排序值(含距离)
     * @param entityInfo     实体信息
     * @param baseSortParams 用户输入的排序参数
     */
    private void setDistance(T entity, List<FieldValue> sortValues, EntityInfo entityInfo, List<BaseSortParam> baseSortParams) {
        List<String> distanceFields = entityInfo.getDistanceFields();
        if (CollectionUtils.isEmpty(distanceFields) || CollectionUtils.isEmpty(sortValues) || CollectionUtils.isEmpty(baseSortParams)) {
            return;
        }

        // 按排序器顺序封装排序字段值
        for (int i = 0, geoFieldIndex = 0; i < sortValues.size(); i++, geoFieldIndex++) {
            if (OrderTypeEnum.GEO != baseSortParams.get(i).getOrderTypeEnum()) {
                // 当前sortValue不是地理位置的排序值，geoFieldIndex不需要变动
                geoFieldIndex--;
                continue;
            }
            FieldValue sortValue = sortValues.get(i);
            if (sortValue == null || sortValue._get() == null) {
                continue;
            }
            double distance = (double)sortValue._get();
            Integer distanceDecimalPlaces = entityInfo.getDistanceDecimalPlaces().get(geoFieldIndex);
            if (distanceDecimalPlaces > ZERO) {
                distance = NumericUtils.setDecimalPlaces(distance, distanceDecimalPlaces);
            }
            try {
                Method invokeMethod = BaseCache.setterMethod(entity.getClass(), distanceFields.get(geoFieldIndex));
                invokeMethod.invoke(entity, distance);
            } catch (Throwable e) {
                // 遇到异常只提示, 不阻断流程 distance未设置不影核心业务
                LogUtils.formatError("set distance error, entity:%s,sortValues:%s,distanceField:%s,e:%s", entity, JsonUtils.toJsonStr(sortValues), distanceFields, e);
            }
        }
    }

    /**
     * 设置查询得分
     *
     * @param entity     实体对象
     * @param score      得分
     * @param entityInfo 实体信息
     */
    private void setScore(T entity, Double score, EntityInfo entityInfo) {
        String scoreField = entityInfo.getScoreField();
        if (Objects.isNull(scoreField) || Objects.isNull(score)) {
            return;
        }

        if (entityInfo.getScoreDecimalPlaces() > ZERO) {
            score = NumericUtils.setDecimalPlaces(score, entityInfo.getScoreDecimalPlaces());
        }

        try {
            Method invokeMethod = BaseCache.setterMethod(entity.getClass(), scoreField);
            invokeMethod.invoke(entity, score);
        } catch (Throwable e) {
            // 遇到异常只提示, 不阻断流程 score未设置不影核心业务
            LogUtils.formatError("set score error, entity:%s,score:%s,scoreField:%s,e:%s", entity, score, scoreField, e);
        }
    }

    /**
     * 获取es搜索响应体
     *
     * @param wrapper 条件
     * @return 搜索响应体
     */
    private SearchResponse<T> getSearchResponse(Wrapper<T> wrapper) {
        return search(wrapper);
    }

    /**
     * 构建创建数据请求参数
     *
     * @param entity    实体
     * @param routing   路由
     * @param parentId  父id
     * @param indexName 索引名
     * @return es请求参数
     */
    private IndexRequest.Builder<T> buildIndexRequest(T entity, String routing, String parentId, String indexName) {
        IndexRequest.Builder<T> indexRequest = new IndexRequest.Builder<T>()
                .routing(routing)
                .index(indexName)
                .document(entity);

        // id预处理,除下述情况,其它情况使用es默认的id
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);

        if (IdType.UUID.equals(entityInfo.getIdType())) {
            indexRequest.id(UUID.randomUUID().toString());
        } else if (IdType.CUSTOMIZE.equals(entityInfo.getIdType())) {
            indexRequest.id(getIdValue(entity));
        }

        // 针对父子类型-追加joinField信息
        if (StringUtils.isNotBlank(entityInfo.getJoinAlias())) {
            if (!(entity instanceof BaseJoin)) {
                throw ExceptionUtils.eee("实体类" + entityClass.getName() + "必须继承BaseJoin实现Join功能");
            }
            BaseJoin b = (BaseJoin) entity;
            b.addJoinField(entityInfo.getJoinFieldName(), entityInfo.getJoinAlias(), entityInfo.isChild() ? parentId : null);
        }

        return indexRequest;
    }

    /**
     * 从es中请求获取searchHit数组
     *
     * @param searchRequest 请求参数
     * @return searchHit数组
     */
    private List<Hit<T>> getSearchHits(SearchRequest searchRequest) {
        PrintUtils.printDsl(searchRequest, client);
        SearchResponse<T> response;
        try {
            response = client.withTransportOptions(getTransportOptions()).search(searchRequest, entityClass);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray exception,searchRequest:%s", e, searchRequest.toString());
        }
        printResponseErrors(response);
        return parseSearchHitArray(response);
    }

    /**
     * 构建更新文档的json
     *
     * @param updateWrapper 条件
     * @return json
     */
    private T buildUpdateDoc(Wrapper<T> updateWrapper) {
        try {
            T t = entityClass.getDeclaredConstructor().newInstance();
            for (EsUpdateParam param : updateWrapper.updateParamList) {
                BaseCache.setterInvoke(entityClass, param.getField(), t, param.getValue());
            }
            return t;
        } catch (Exception e) {
            throw ExceptionUtils.eee("buildUpdateDoc Exception, updateWrapper:%s", e, updateWrapper.toString());
        }
    }

    /**
     * 执行bulk请求,并返回成功个数
     *
     * @param bulkRequest 批量请求参数
     * @return 成功个数
     */
    private Integer doBulkRequest(BulkRequest bulkRequest, String successResult) {
        PrintUtils.printDsl(bulkRequest, client);
        try {
            BulkResponse bulkResponse = client.withTransportOptions(getTransportOptions()).bulk(bulkRequest);
            if (bulkResponse.errors()) {
                LogUtils.error(bulkResponse.toString());
            }

            return (int) bulkResponse.items().stream()
                    .filter(item -> Objects.equals(item.result(), successResult))
                    .count();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.error("bulk request exception");
            return 0;
        }
    }

    /**
     * 从es中请求获取searchHit数组
     *
     * @param wrapper 参数包装类
     * @return searchHit数组
     */
    private List<Hit<T>> getSearchHits(Wrapper<T> wrapper) {
        // 用户在wrapper中指定的混合查询条件优先级最高
        SearchRequest searchRequest = Optional.ofNullable(wrapper.searchBuilder)
                .orElse(WrapperProcessor.buildSearchBuilder(wrapper, entityClass))
                .index(WrapperProcessor.getIndexName(entityClass, wrapper.indexNames))
                .routing(wrapper.routing)
                .preference(wrapper.preference)
                .build();

        PrintUtils.printDsl(searchRequest, client);
        SearchResponse<T> response;
        try {
            response = client.withTransportOptions(getTransportOptions()).search(searchRequest, entityClass);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray IOException, searchRequest:%s", e, searchRequest.toString());
        }
        printResponseErrors(response);
        return parseSearchHitArray(response);
    }

    /**
     * 从ES返回结果中解析出SearchHit[]
     *
     * @param searchResponse es返回的响应体
     * @return 响应体中的Hit列表
     */
    private List<Hit<T>> parseSearchHitArray(SearchResponse<T> searchResponse) {
        return Optional.ofNullable(searchResponse)
                .map(SearchResponse::hits)
                .map(HitsMetadata::hits)
                .orElseThrow(() -> ExceptionUtils.eee("parseSearchHitArray exception"));
    }

    /**
     * 获取id实际字段名称
     *
     * @return id实际字段名称
     */
    private String getRealIdFieldName() {
        return EntityInfoHelper.getEntityInfo(entityClass).getKeyProperty();
    }

    /**
     * 获取id
     *
     * @param obj 实体
     * @return id
     */
    private String getId(Object obj) {
        Object id = BaseCache.getId(entityClass, obj);
        return id == null ? null : id.toString();
    }

    /**
     * 获取表字段->高亮返回结果 键值对
     *
     * @return 表字段->高亮返回结果 map
     */
    private Map<String, String> getHighlightFieldMap() {
        return EntityInfoHelper.getEntityInfo(entityClass).getHighlightFieldMap();
    }

    /**
     * 设置高亮字段的值
     *
     * @param entity         实体类
     * @param highlightField 高亮返回字段
     * @param value          高亮结果值
     */
    private void setHighlightValue(Object entity, String highlightField, String value) {
        try {
            Method invokeMethod = BaseCache.setterMethod(entity.getClass(), highlightField);
            invokeMethod.invoke(entity, value);
        } catch (Throwable e) {
            LogUtils.formatError("setHighlightValue error,entity:%s,highlightField:%s,value:%s,e:%s",
                    entity.toString(), highlightField, value, e.toString());
        }
    }

    /**
     * 设置id值
     *
     * @param entity 实体
     * @param id     主键
     */
    private void setId(Object entity, String id) {
        try {
            Method invokeMethod = BaseCache.setterMethod(entity.getClass(), getRealIdFieldName());

            // 将es返回的String类型id还原为字段实际的id类型,比如Long,否则反射会报错
            Class<?> idClass = EntityInfoHelper.getEntityInfo(entity.getClass()).getIdClass();
            Object val = ReflectionKit.getVal(id, idClass);
            invokeMethod.invoke(entity, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取实体对象的id值
     *
     * @param entity 实体对象
     * @return id值
     */
    private String getIdValue(T entity) {
        try {
            EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
            Field keyField = Optional.ofNullable(entityInfo.getKeyField())
                    .orElseThrow(() -> ExceptionUtils.eee("the entity id field not found"));
            Object value = keyField.get(entity);
            return Optional.ofNullable(value)
                    .map(Object::toString)
                    .orElseThrow(() -> ExceptionUtils.eee("the entity id must not be null"));
        } catch (IllegalAccessException e) {
            throw ExceptionUtils.eee("get id value exception", e);
        }
    }

    /**
     * 对响应结构进行判断，如果有错误，则抛出异常
     *
     * <p>如下，client方法都需要判定</p>
     * client.search
     * client.scroll
     * client.explain 等等
     */
    private void printResponseErrors(ResponseBody<T> searchResponse) {
        if (Objects.nonNull(searchResponse)
            && searchResponse.shards().failures() != null
            && searchResponse.shards().failures().size() > ZERO) {
            String errorMsg = searchResponse.shards().failures().get(0).toString();
            throw ExceptionUtils.eee("search response failed ,failedShards: " + errorMsg);
        }
    }

    /**
     * 获取刷新策略
     *
     * @return 刷新策略
     */
    private Refresh getRefreshPolicy() {
        // 防止傻狍子用户在全局中把刷新策略修改为GLOBAL
        final RefreshPolicy refreshPolicy = Optional.ofNullable(EntityInfoHelper.getEntityInfo(entityClass).getRefreshPolicy()).orElse(RefreshPolicy.NONE);
        switch (refreshPolicy) {
            case IMMEDIATE:
                return Refresh.True;
            case WAIT_UNTIL:
                return Refresh.WaitFor;
            default:
                return Refresh.False;
        }
    }

    /**
     * 获取请求配置
     *
     * @return 请求配置
     */
    private TransportOptions getTransportOptions() {
        return EntityInfoHelper.getEntityInfo(entityClass).getRequestOptions();
    }

}
