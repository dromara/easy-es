package org.dromara.easyes.core.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.http.util.EntityUtils;
import org.dromara.easyes.annotation.rely.FieldStrategy;
import org.dromara.easyes.annotation.rely.IdType;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.common.enums.EsQueryTypeEnum;
import org.dromara.easyes.common.enums.MethodEnum;
import org.dromara.easyes.common.utils.*;
import org.dromara.easyes.core.biz.*;
import org.dromara.easyes.core.cache.BaseCache;
import org.dromara.easyes.core.cache.GlobalConfigCache;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.FieldUtils;
import org.dromara.easyes.core.toolkit.IndexUtils;
import org.dromara.easyes.core.toolkit.PageHelper;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

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
 * 核心网络请求类：{@link RestHighLevelClient}、
 * 动态封装request类：{@link WrapperProcessor}、
 * 查询类型枚举：{@link EsQueryTypeEnum}、
 * </p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public class BaseEsMapperImpl<T> implements BaseEsMapper<T> {
    /**
     * restHighLevel client
     */
    @Setter
    private RestHighLevelClient client;
    /**
     * T 对应的类
     */
    @Setter
    private Class<T> entityClass;

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
        return IndexUtils.getIndex(client, getIndexName(indexName));
    }

    @Override
    public Boolean createIndex() {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo);
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

    @Override
    public Boolean createIndex(String indexName) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        CreateIndexParam createIndexParam = IndexUtils.getCreateIndexParam(entityInfo);
        if (StringUtils.isNotBlank(indexName)) {
            createIndexParam.setIndexName(indexName);
        }
        return IndexUtils.createIndex(client, entityInfo, createIndexParam);
    }

    @Override
    public Boolean createIndex(Wrapper<T> wrapper) {
        Arrays.stream(wrapper.indexNames).forEach(indexName -> doCreateIndex(wrapper, indexName));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateIndex(Wrapper<T> wrapper) {
        Arrays.stream(wrapper.indexNames).forEach(indexName -> doUpdateIndex(wrapper, indexName));
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
    public Boolean refresh() {
        return this.refresh(EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Boolean refresh(String... indexNames) {
        RefreshRequest request = new RefreshRequest(indexNames);
        try {
            RefreshResponse refresh = client.indices().refresh(request, RequestOptions.DEFAULT);
            return refresh.getSuccessfulShards() == Arrays.stream(indexNames).count();
        } catch (IOException e) {
            e.printStackTrace();
            throw ExceptionUtils.eee("refresh index exception e", e);
        }
    }

    @Override
    @SneakyThrows
    public String executeSQL(String sql) {
        Request request = new Request(MethodEnum.POST.name(), SQL_ENDPOINT);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(QUERY, sql);
        request.setJsonEntity(jsonObject.toJSONString());
        Response response = client.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    @SneakyThrows
    public String executeDSL(String dsl) {
        return executeDSL(dsl, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    @SneakyThrows
    public String executeDSL(String dsl, String indexName) {
        Assert.notNull(indexName, "indexName must not null");
        Request request = new Request(MethodEnum.GET.name(), indexName + DSL_ENDPOINT);
        Response response = client.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public SearchResponse search(Wrapper<T> wrapper) {
        // 执行普通混合查询, 不含searchAfter分页
        return getSearchResponse(wrapper, null);
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest, RequestOptions requestOptions) throws IOException {
        printDSL(searchRequest);
        SearchResponse response = client.search(searchRequest, requestOptions);
        printResponseErrors(response);
        return response;
    }

    @Override
    public SearchResponse scroll(SearchScrollRequest searchScrollRequest, RequestOptions requestOptions) throws IOException {
        SearchResponse response = client.scroll(searchScrollRequest, requestOptions);
        printResponseErrors(response);
        return response;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(Wrapper<T> wrapper) {
        return WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
    }

    @Override
    public String getSource(Wrapper<T> wrapper) {
        // 获取由本框架生成的es查询参数 用于验证生成语法的正确性
        SearchRequest searchRequest = new SearchRequest(getIndexNames(wrapper.indexNames));
        Optional.ofNullable(getRouting()).ifPresent(searchRequest::routing);
        SearchSourceBuilder searchSourceBuilder = WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        return Optional.ofNullable(searchRequest.source())
                .map(SearchSourceBuilder::toString)
                .orElseThrow(() -> ExceptionUtils.eee("get search source exception"));
    }

    @Override
    public EsPageInfo<T> pageQuery(Wrapper<T> wrapper, Integer pageNum, Integer pageSize) {
        // 兼容分页参数
        pageNum = pageNum == null || pageNum <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_NUM : pageNum;
        pageSize = pageSize == null || pageSize <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;

        wrapper.from = (pageNum - 1) * pageSize;
        wrapper.size = pageSize;

        // 请求es获取数据
        SearchResponse response = getSearchResponse(wrapper);

        // 解析数据
        SearchHit[] searchHits = parseSearchHitArray(response);
        List<T> dataList = Arrays.stream(searchHits)
                .map(searchHit -> parseOne(searchHit, wrapper))
                .collect(Collectors.toList());
        long count = parseCount(response, Objects.nonNull(wrapper.distinctField));
        return PageHelper.getPageInfo(dataList, count, pageNum, pageSize);
    }

    @Override
    public SAPageInfo<T> searchAfterPage(Wrapper<T> wrapper, List<Object> searchAfter, Integer pageSize) {
        // searchAfter语法规定 或from只允许为0或-1或不传,否则es会报错, 推荐不指定, 直接传null即可
        boolean illegalArg = Objects.nonNull(wrapper.from) && (!wrapper.from.equals(ZERO) || !wrapper.from.equals(MINUS_ONE));
        if (illegalArg) {
            throw ExceptionUtils.eee("The wrapper.from in searchAfter must be 0 or -1 or null, null is recommended");
        }

        // searchAfter必须要进行排序，不排序无法进行分页
        boolean notSort = CollectionUtils.isEmpty(wrapper.baseSortParams) && CollectionUtils.isEmpty(wrapper.orderByParams);
        if (notSort) {
            throw ExceptionUtils.eee("sortParamList cannot be empty");
        }

        // 兼容分页参数
        pageSize = pageSize == null || pageSize <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;
        wrapper.size = pageSize;

        // 请求es获取数据
        SearchResponse response =
                CollectionUtils.isEmpty(searchAfter) ? getSearchResponse(wrapper) : getSearchResponse(wrapper, searchAfter.toArray());

        // 解析数据
        SearchHit[] searchHits = parseSearchHitArray(response);
        List<T> dataList = Arrays.stream(searchHits).map(searchHit -> parseOne(searchHit, wrapper))
                .collect(Collectors.toList());
        Object[] nextSearchAfter = Arrays.stream(searchHits)
                .map(SearchHit::getSortValues)
                .reduce((first, second) -> second)
                .orElse(null);
        long count = parseCount(response, Objects.nonNull(wrapper.distinctField));
        return PageHelper.getSAPageInfo(dataList, count, searchAfter,
                nextSearchAfter == null ? null : Arrays.asList(nextSearchAfter), pageSize);
    }

    @Override
    public Long selectCount(Wrapper<T> wrapper) {
        return selectCount(wrapper, Objects.nonNull(wrapper.distinctField));
    }

    @Override
    public Long selectCount(Wrapper<T> wrapper, boolean distinct) {
        if (distinct) {
            // 去重, 总数来源于桶, 只查id列,节省内存 拷贝是防止追加的只查id列影响到count后的其它查询
            Wrapper<T> clone = wrapper.clone();
            clone.include = new String[]{DEFAULT_ES_ID_NAME};
            SearchResponse response = getSearchResponse(clone);
            return parseCount(response, Objects.nonNull(clone.distinctField));
        } else {
            // 不去重,直接count获取,效率更高
            CountRequest countRequest = new CountRequest(getIndexNames(wrapper.indexNames));
            Optional.ofNullable(getRouting()).ifPresent(countRequest::routing);
            QueryBuilder queryBuilder = Optional.ofNullable(wrapper.searchSourceBuilder)
                    .map(SearchSourceBuilder::query)
                    .orElseGet(() -> WrapperProcessor.initBoolQueryBuilder(wrapper.paramQueue, entityClass));
            countRequest.query(queryBuilder);
            CountResponse count;
            try {
                printCountDSL(countRequest);
                count = client.count(countRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw ExceptionUtils.eee("selectCount exception", e);
            }
            return count.getCount();
        }
    }

    @Override
    public Integer insert(T entity) {
        Assert.notNull(entity, "insert entity must not be null");
        return insert(entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insert(T entity, String... indexNames) {
        Assert.notNull(entity, "insert entity must not be null");

        // 执行插入
        return Arrays.stream(getIndexNames(indexNames))
                .mapToInt(indexName -> doInsert(entity, indexName))
                .sum();
    }

    @Override
    public Integer insertBatch(Collection<T> entityList) {
        return insertBatch(entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insertBatch(Collection<T> entityList, String... indexNames) {
        // 裤子都脱了 你告诉我没有数据 怎么*入?
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }

        // 在每条指定的索引上批量执行数据插入
        return Arrays.stream(getIndexNames(indexNames))
                .mapToInt(indexName -> doInsertBatch(entityList, indexName))
                .sum();
    }

    @Override
    public Integer deleteById(Serializable id) {
        return deleteById(id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteById(Serializable id, String... indexNames) {
        return Arrays.stream(getIndexNames(indexNames))
                .mapToInt(indexName -> doDeleteById(id, indexName))
                .sum();
    }

    @Override
    public Integer deleteBatchIds(Collection<? extends Serializable> idList) {
        return deleteBatchIds(idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteBatchIds(Collection<? extends Serializable> idList, String... indexNames) {
        Assert.notEmpty(idList, "the collection of id must not empty");
        return Arrays.stream(getIndexNames(indexNames))
                .mapToInt(indexName -> doDeleteBatchIds(idList, indexName))
                .sum();
    }

    @Override
    public Integer delete(Wrapper<T> wrapper) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(getIndexNames(wrapper.indexNames));
        Optional.ofNullable(getRouting()).ifPresent(request::setRouting);
        BoolQueryBuilder boolQueryBuilder = WrapperProcessor.initBoolQueryBuilder(wrapper.paramQueue, entityClass);
        request.setQuery(boolQueryBuilder);
        BulkByScrollResponse bulkResponse;
        try {
            bulkResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("delete error, dsl:%s", e, boolQueryBuilder.toString());
        }
        // 单次删除通常不会超过21亿, 这里为了兼容老API设计,仍转为int 2.0版本将调整为long
        return (int) bulkResponse.getDeleted();
    }

    @Override
    public Integer updateById(T entity) {
        return updateById(entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateById(T entity, String... indexNames) {
        Assert.notNull(entity, "entity must not be null");

        // 获取id值
        String idValue = getIdValue(entity);

        // 在每条索引上执行更新
        return Arrays.stream(getIndexNames(indexNames))
                .mapToInt(indexName -> doUpdateById(entity, idValue, indexName))
                .sum();
    }

    @Override
    public Integer updateBatchByIds(Collection<T> entityList) {
        return updateBatchByIds(entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateBatchByIds(Collection<T> entityList, String... indexNames) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }

        // 在每条指定索引上批量执行更新
        return Arrays.stream(getIndexNames(indexNames))
                .mapToInt(indexName -> doUpdateBatchByIds(entityList, indexName))
                .sum();
    }

    @Override
    public Integer update(T entity, Wrapper<T> updateWrapper) {
        if (Objects.isNull(entity) && CollectionUtils.isEmpty(updateWrapper.updateParamList)) {
            return BaseEsConstants.ZERO;
        }

        // 在每条指定索引上执行更新操作
        return Arrays.stream(getIndexNames(updateWrapper.indexNames))
                .mapToInt(indexName -> doUpdate(entity, updateWrapper, indexName))
                .sum();
    }

    @Override
    public T selectById(Serializable id) {
        return selectById(id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public T selectById(Serializable id, String... indexNames) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }

        // 从指定的多条索引上去获取, 返回最先命中的数据
        return Arrays.stream(getIndexNames(indexNames))
                .map(indexName -> doSelectById(id, indexName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList) {
        return selectBatchIds(idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList, String... indexNames) {
        Assert.notEmpty(idList, "id collection must not be null or empty");

        // 在每条指定索引上执行查询
        List<T> result = new ArrayList<>();
        Arrays.stream(getIndexNames(indexNames))
                .forEach(indexName -> result.addAll(doSelectBatchIds(idList, indexName)));
        return result;
    }

    private List<T> doSelectBatchIds(Collection<? extends Serializable> idList, String indexName) {
        // 构造查询参数
        List<String> stringIdList = idList.stream().map(Object::toString).collect(Collectors.toList());
        SearchRequest searchRequest = new SearchRequest(getIndexNames(indexName));
        Optional.ofNullable(getRouting()).ifPresent(searchRequest::routing);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termsQuery(DEFAULT_ES_ID_NAME, stringIdList));
        sourceBuilder.size(idList.size());
        searchRequest.source(sourceBuilder);

        // 请求es获取数据
        SearchHit[] searchHitArray = getSearchHitArray(searchRequest);
        if (ArrayUtils.isEmpty(searchHitArray)) {
            return new ArrayList<>(0);
        }

        // 批量解析数据
        return Arrays.stream(searchHitArray)
                .map(this::parseOne)
                .collect(Collectors.toList());
    }

    @Override
    public T selectOne(Wrapper<T> wrapper) {
        // 请求es获取数据
        SearchResponse searchResponse = getSearchResponse(wrapper);
        long count = parseCount(searchResponse, Objects.nonNull(wrapper.distinctField));
        boolean invalid = (count > ONE && (Objects.nonNull(wrapper.size) && wrapper.size > ONE))
                || (count > ONE && Objects.isNull(wrapper.size));
        if (invalid) {
            LogUtils.error("found more than one result:" + count, "please use wrapper.limit to limit 1");
            throw ExceptionUtils.eee("found more than one result: %d, please use wrapper.limit to limit 1", count);
        }

        // 解析数据
        SearchHit[] searchHits = parseSearchHitArray(searchResponse);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }
        return parseOne(searchHits[0], wrapper);
    }

    @Override
    public List<T> selectList(Wrapper<T> wrapper) {
        // 请求es获取数据
        SearchHit[] searchHits = getSearchHitArray(wrapper);
        if (ArrayUtils.isEmpty(searchHits)) {
            return new ArrayList<>(0);
        }

        // 批量解析
        return Arrays.stream(searchHits)
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

        // 设置分片个副本信息
        Optional.ofNullable(wrapper.shardsNum).ifPresent(createIndexParam::setShardsNum);
        Optional.ofNullable(wrapper.replicasNum).ifPresent(createIndexParam::setReplicasNum);
        Optional.ofNullable(wrapper.maxResultWindow).ifPresent(createIndexParam::setMaxResultWindow);

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

        // 更新mapping
        PutMappingRequest putMappingRequest = new PutMappingRequest(indexName);

        if (Objects.isNull(wrapper.mapping)) {
            Assert.notEmpty(wrapper.esIndexParamList, String.format("update index: %s failed, because of empty update args", indexName));
            Map<String, Object> mapping = IndexUtils.initMapping(EntityInfoHelper.getEntityInfo(entityClass), wrapper.esIndexParamList);
            putMappingRequest.source(mapping);
        } else {
            // 用户自行指定的mapping信息
            putMappingRequest.source(wrapper.mapping);
        }

        try {
            AcknowledgedResponse acknowledgedResponse = client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
            Assert.isTrue(acknowledgedResponse.isAcknowledged(), String.format("update index failed, index: %s", indexName));
        } catch (IOException e) {
            throw ExceptionUtils.eee("update index exception", e);
        }
    }

    /**
     * 执行插入单条数据
     *
     * @param entity    插入对象
     * @param indexName 索引名
     * @return 成功条数
     */
    private Integer doInsert(T entity, String indexName) {
        // 构建请求入参
        IndexRequest indexRequest = buildIndexRequest(entity, indexName);
        Optional.ofNullable(getRouting()).ifPresent(indexRequest::routing);
        indexRequest.setRefreshPolicy(getRefreshPolicy());

        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            if (Objects.equals(indexResponse.status(), RestStatus.CREATED)) {
                setId(entity, indexResponse.getId());
                return BaseEsConstants.ONE;
            } else if (Objects.equals(indexResponse.status(), RestStatus.OK)) {
                // 该id已存在,数据被更新的情况
                return BaseEsConstants.ZERO;
            } else {
                throw ExceptionUtils.eee("insert failed, result:%s entity:%s", indexResponse.getResult(), entity);
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("insert entity:%s exception", e, entity.toString());
        }
    }

    /**
     * 执行批量插入数据
     *
     * @param entityList 数据列表
     * @param indexName  索引名
     * @return 总成功条数
     */
    private Integer doInsertBatch(Collection<T> entityList, String indexName) {
        // 构建批量请求参数
        BulkRequest bulkRequest = new BulkRequest();
        Optional.ofNullable(getRouting()).ifPresent(bulkRequest::routing);
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        entityList.forEach(entity -> {
            IndexRequest indexRequest = buildIndexRequest(entity, indexName);
            bulkRequest.add(indexRequest);
        });

        // 执行批量请求并返回结果
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT, entityList);
    }


    /**
     * 执行根据id删除指定数据
     *
     * @param id        id
     * @param indexName 索引名
     * @return 成功条数
     */
    private Integer doDeleteById(Serializable id, String indexName) {
        DeleteRequest deleteRequest = generateDelRequest(id, indexName);
        Optional.ofNullable(getRouting()).ifPresent(deleteRequest::routing);
        deleteRequest.setRefreshPolicy(getRefreshPolicy());
        try {
            DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
            if (Objects.equals(deleteResponse.status(), RestStatus.OK)) {
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
     * @param indexName 索引名
     * @return 总成功条数
     */
    private Integer doDeleteBatchIds(Collection<? extends Serializable> idList, String indexName) {
        BulkRequest bulkRequest = new BulkRequest();
        Optional.ofNullable(getRouting()).ifPresent(bulkRequest::routing);
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        idList.forEach(id -> {
            DeleteRequest deleteRequest = generateDelRequest(id, indexName);
            bulkRequest.add(deleteRequest);
        });
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 执行根据id更新
     *
     * @param entity    更新数据
     * @param idValue   id值
     * @param indexName 索引名
     * @return 更新条数
     */
    private Integer doUpdateById(T entity, String idValue, String indexName) {
        // 构建更新请求参数
        UpdateRequest updateRequest = buildUpdateRequest(entity, idValue, indexName);
        Optional.ofNullable(getRouting()).ifPresent(updateRequest::routing);
        updateRequest.setRefreshPolicy(getRefreshPolicy());

        // 执行更新
        try {
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            if (Objects.equals(updateResponse.status(), RestStatus.OK)) {
                return BaseEsConstants.ONE;
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("updateById exception,entity:%s", e, entity.toString());
        }

        return BaseEsConstants.ZERO;
    }

    /**
     * 执行根据id批量更新
     *
     * @param entityList 更新数据列表
     * @param indexName  索引名
     * @return 总成功条数
     */
    private Integer doUpdateBatchByIds(Collection<T> entityList, String indexName) {
        // 封装批量请求参数
        BulkRequest bulkRequest = new BulkRequest();
        Optional.ofNullable(getRouting()).ifPresent(bulkRequest::routing);
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        entityList.forEach(entity -> {
            String idValue = getIdValue(entity);
            UpdateRequest updateRequest = buildUpdateRequest(entity, idValue, indexName);
            bulkRequest.add(updateRequest);
        });

        // 执行批量请求
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
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
        String jsonData = Optional.ofNullable(entity)
                .map(this::buildJsonIndexSource)
                .orElseGet(() -> buildJsonDoc(updateWrapper));

        // 批量更新
        BulkRequest bulkRequest = new BulkRequest();
        Optional.ofNullable(getRouting()).ifPresent(bulkRequest::routing);
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        Method getId = BaseCache.getterMethod(entityClass, getRealIdFieldName());
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        list.forEach(item -> {
            UpdateRequest updateRequest = new UpdateRequest();
            try {
                Object invoke = getId.invoke(item);
                Optional.ofNullable(invoke).ifPresent(id -> updateRequest.id(id.toString()));
            } catch (Exception e) {
                throw ExceptionUtils.eee("update exception", e);
            }
            updateRequest.index(indexName);
            updateRequest.doc(jsonData, XContentType.JSON);

            // 父子类型-子文档,追加其路由,否则无法更新
            if (entityInfo.isChild()) {
                String routing = getRouting(item, entityInfo.getJoinFieldClass());
                updateRequest.routing(routing);
            }
            bulkRequest.add(updateRequest);
        });
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 执行根据id查询
     *
     * @param id        id
     * @param indexName 索引名
     * @return 数据
     */
    private T doSelectById(Serializable id, String indexName) {
        // 构造查询参数
        SearchRequest searchRequest = new SearchRequest(indexName);
        Optional.ofNullable(getRouting()).ifPresent(searchRequest::routing);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(DEFAULT_ES_ID_NAME, id));
        searchRequest.source(searchSourceBuilder);

        // 请求es获取数据
        SearchHit[] searchHits = getSearchHitArray(searchRequest);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }

        // 解析数据并返回
        return parseOne(searchHits[0]);
    }


    /**
     * 获取es查询结果返回体
     *
     * @param wrapper     条件
     * @param searchAfter searchAfter参数
     * @return es返回体
     */
    private SearchResponse getSearchResponse(Wrapper<T> wrapper, Object[] searchAfter) {
        // 构建es restHighLevelClient 查询参数
        SearchRequest searchRequest = new SearchRequest(getIndexNames(wrapper.indexNames));
        Optional.ofNullable(getRouting()).ifPresent(searchRequest::routing);

        // 用户在wrapper中指定的混合查询条件优先级最高
        SearchSourceBuilder searchSourceBuilder = Optional.ofNullable(wrapper.searchSourceBuilder)
                .map(builder -> {
                    // 兼容混合查询时用户在分页中自定义的分页参数
                    Optional.ofNullable(wrapper.from).ifPresent(builder::from);
                    Optional.ofNullable(wrapper.size).ifPresent(builder::size);
                    return builder;
                }).orElseGet(() -> WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass));
        searchRequest.source(searchSourceBuilder);
        Optional.ofNullable(searchAfter).ifPresent(searchSourceBuilder::searchAfter);
        printDSL(searchRequest);

        // 执行查询
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("search exception", e);
        }
        printResponseErrors(response);
        return response;
    }


    /**
     * 生成DelRequest请求参数
     *
     * @param id        id
     * @param indexName 索引名
     * @return DelRequest
     */
    private DeleteRequest generateDelRequest(Serializable id, String indexName) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.id(id.toString());
        deleteRequest.index(indexName);
        return deleteRequest;
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
        SearchRequest searchRequest = new SearchRequest(indexName);
        Optional.ofNullable(getRouting()).ifPresent(searchRequest::routing);
        SearchSourceBuilder searchSourceBuilder;
        if (Objects.isNull(wrapper.searchSourceBuilder)) {
            searchSourceBuilder = new SearchSourceBuilder();
            // 只查id列,节省内存
            String[] includes = {DEFAULT_ES_ID_NAME};
            EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
            if (Objects.nonNull(entityInfo) && entityInfo.isChild()) {
                // 父子类型,须追加joinField 否则无法获取其路由
                Arrays.fill(includes, entityInfo.getJoinFieldName());
            }
            searchSourceBuilder.fetchSource(includes, null);
            searchSourceBuilder.trackTotalHits(true);
            searchSourceBuilder.size(GlobalConfigCache.getGlobalConfig().getDbConfig().getBatchUpdateThreshold());
            BoolQueryBuilder boolQueryBuilder = WrapperProcessor.initBoolQueryBuilder(wrapper.paramQueue, entityClass);
            searchSourceBuilder.query(boolQueryBuilder);
        } else {
            // 用户在wrapper中指定的混合查询条件优先级最高
            searchSourceBuilder = wrapper.searchSourceBuilder;
        }
        searchRequest.source(searchSourceBuilder);
        printDSL(searchRequest);
        try {
            // 查询数据明细
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            printResponseErrors(response);
            SearchHit[] searchHits = parseSearchHitArray(response);
            return Arrays.stream(searchHits)
                    .map(this::parseOne)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw ExceptionUtils.eee("selectIdList exception", e);
        }
    }

    /**
     * 构建创建数据请求参数
     *
     * @param entity    实体
     * @param indexName 索引名
     * @return es请求参数
     */
    private IndexRequest buildIndexRequest(T entity, String indexName) {
        IndexRequest indexRequest = new IndexRequest();

        // id预处理,除下述情况,其它情况使用es默认的id
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);

        if (IdType.UUID.equals(entityInfo.getIdType())) {
            indexRequest.id(UUID.randomUUID().toString());
        } else if (IdType.CUSTOMIZE.equals(entityInfo.getIdType())) {
            indexRequest.id(getIdValue(entity));
        }

        // 构建插入的json格式数据
        String jsonData = buildJsonIndexSource(entity);

        indexRequest.index(indexName);
        indexRequest.source(jsonData, XContentType.JSON);

        // 父子类型-子文档,设置路由
        if (entityInfo.isChild()) {
            String routing = getRouting(entity, entityInfo.getJoinFieldClass());
            indexRequest.routing(routing);
        }

        return indexRequest;
    }

    /**
     * 构建更新数据请求参数
     *
     * @param entity    实体
     * @param idValue   id值
     * @param indexName 索引名
     * @return 更新请求参数
     */
    private UpdateRequest buildUpdateRequest(T entity, String idValue, String indexName) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.id(idValue);
        updateRequest.index(indexName);
        String jsonData = buildJsonIndexSource(entity);
        updateRequest.doc(jsonData, XContentType.JSON);
        return updateRequest;
    }

    /**
     * 解析获取据数总数
     *
     * @param response es返回的数据
     * @param distinct 是否去重统计
     * @return 总数
     */
    private long parseCount(SearchResponse response, boolean distinct) {
        AtomicLong repeatNum = new AtomicLong(0);
        if (distinct) {
            Optional.ofNullable(response.getAggregations())
                    .ifPresent(aggregations -> {
                        ParsedCardinality parsedCardinality = aggregations.get(BaseEsConstants.REPEAT_NUM_KEY);
                        Optional.ofNullable(parsedCardinality).ifPresent(p -> repeatNum.getAndAdd(p.getValue()));
                    });
        } else {
            Optional.ofNullable(response.getHits())
                    .flatMap(searchHits -> Optional.ofNullable(searchHits.getTotalHits()))
                    .ifPresent(totalHits -> repeatNum.getAndAdd(totalHits.value));
        }
        return repeatNum.get();
    }

    /**
     * 从searchHit中解析一条数据
     *
     * @param searchHit es返回数据
     * @return 实际想要的数据
     */
    private T parseOne(SearchHit searchHit) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        T entity = JSON.parseObject(searchHit.getSourceAsString(), entityClass, entityInfo.getExtraProcessor());

        // id字段处理
        setId(entity, searchHit.getId());

        // 得分字段处理
        setScore(entity, searchHit.getScore(), entityInfo);

        // 距离字段处理
        setDistance(entity, searchHit.getSortValues(), entityInfo);

        return entity;
    }

    /**
     * 从searchHit中解析一条数据
     *
     * @param searchHit es返回数据
     * @param wrapper   参数包装类
     * @return 实际想要的数据
     */
    private T parseOne(SearchHit searchHit, Wrapper<T> wrapper) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        // 解析json
        T entity = JSON.parseObject(searchHit.getSourceAsString(), entityClass, entityInfo.getExtraProcessor());

        // 高亮字段处理
        if (CollectionUtils.isNotEmpty(entityInfo.getHighLightParams())) {
            Map<String, String> highlightFieldMap = getHighlightFieldMap();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            highlightFields.forEach((key, value) -> {
                String highLightValue = Arrays.stream(value.getFragments()).findFirst().map(Text::string).orElse(BaseEsConstants.EMPTY_STR);
                setHighlightValue(entity, highlightFieldMap.get(key), highLightValue);
            });
        }

        // 得分字段处理
        setScore(entity, searchHit.getScore(), entityInfo);

        // 距离字段处理
        setDistance(entity, searchHit.getSortValues(), entityInfo);

        // id处理
        boolean includeId = WrapperProcessor.includeId(getRealIdFieldName(), wrapper);
        if (includeId) {
            setId(entity, searchHit.getId());
        }

        return entity;
    }

    /**
     * 设置距离
     *
     * @param entity     实体对象
     * @param sortValues 排序值(含距离)
     * @param entityInfo 实体信息
     */
    private void setDistance(T entity, Object[] sortValues, EntityInfo entityInfo) {
        List<String> distanceFields = entityInfo.getDistanceFields();
        if (CollectionUtils.isEmpty(distanceFields) || ArrayUtils.isEmpty(sortValues)) {
            return;
        }

        // 按排序器顺序封装排序字段值
        for (int i = 0; i < sortValues.length; i++) {
            Object sortValue = sortValues[i];
            if (!(sortValue instanceof Double)) {
                continue;
            }
            double distance = (double) sortValue;
            if (Double.isNaN(distance)) {
                continue;
            }
            Integer distanceDecimalPlaces = entityInfo.getDistanceDecimalPlaces().get(i);
            if (distanceDecimalPlaces > ZERO) {
                distance = NumericUtils.setDecimalPlaces(distance, distanceDecimalPlaces);
            }
            try {
                Method invokeMethod = BaseCache.setterMethod(entity.getClass(), distanceFields.get(i));
                invokeMethod.invoke(entity, distance);
            } catch (Throwable e) {
                // 遇到异常只提示, 不阻断流程 distance未设置不影核心业务
                LogUtils.formatError("set distance error, entity:%s,sortValues:%s,distanceField:%s,e:%s", entity, JSON.toJSONString(sortValues), distanceFields, e);
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
    private void setScore(T entity, float score, EntityInfo entityInfo) {
        String scoreField = entityInfo.getScoreField();
        if (Objects.isNull(scoreField) || Float.isNaN(score)) {
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
    private SearchResponse getSearchResponse(Wrapper<T> wrapper) {
        return search(wrapper);
    }

    /**
     * 从es中请求获取searchHit数组
     *
     * @param searchRequest 请求参数
     * @return searchHit数组
     */
    private SearchHit[] getSearchHitArray(SearchRequest searchRequest) {
        printDSL(searchRequest);
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray exception,searchRequest:%s", e, searchRequest.toString());
        }
        printResponseErrors(response);
        return parseSearchHitArray(response);
    }


    /**
     * 从es中请求获取searchHit数组
     *
     * @param wrapper 参数包装类
     * @return searchHit数组
     */
    private SearchHit[] getSearchHitArray(Wrapper<T> wrapper) {
        SearchRequest searchRequest = new SearchRequest(getIndexNames(wrapper.indexNames));
        Optional.ofNullable(getRouting()).ifPresent(searchRequest::routing);

        // 用户在wrapper中指定的混合查询条件优先级最高
        SearchSourceBuilder searchSourceBuilder = Objects.isNull(wrapper.searchSourceBuilder) ?
                WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass) : wrapper.searchSourceBuilder;
        searchRequest.source(searchSourceBuilder);
        printDSL(searchRequest);
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray IOException, searchRequest:%s", e, searchRequest.toString());
        }
        printResponseErrors(response);
        return parseSearchHitArray(response);
    }

    /**
     * 构建,插入/更新 的JSON对象
     *
     * @param entity 实体
     * @return json
     */
    private String buildJsonIndexSource(T entity) {
        // 获取当前类所有字段列表
        // 根据字段配置的策略 决定是否加入到实际es处理字段中
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        List<EntityFieldInfo> fieldList = entityInfo.getFieldList();
        Set<String> excludeColumn = new HashSet<>();

        fieldList.forEach(field -> {
            String column = field.getColumn();
            Method invokeMethod = BaseCache.getterMethod(entityClass, column);
            Object invoke;
            FieldStrategy fieldStrategy = field.getFieldStrategy();
            try {
                if (FieldStrategy.NOT_NULL.equals(fieldStrategy)) {
                    invoke = invokeMethod.invoke(entity);
                    if (Objects.isNull(invoke)) {
                        excludeColumn.add(column);
                    }
                } else if (FieldStrategy.NOT_EMPTY.equals(fieldStrategy)) {
                    invoke = invokeMethod.invoke(entity);
                    if (Objects.isNull(invoke)) {
                        excludeColumn.add(column);
                    } else {
                        if (invoke instanceof String) {
                            String strValue = (String) invoke;
                            if (StringUtils.isEmpty(strValue)) {
                                excludeColumn.add(column);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw ExceptionUtils.eee("buildJsonIndexSource exception, entity:%s", e, entity.toString());
            }
        });

        // 字段过滤器
        List<SerializeFilter> serializeFilters = new ArrayList<>();
        Optional.ofNullable(entityInfo.getClassSimplePropertyPreFilterMap().get(entityClass))
                .ifPresent(serializeFilters::addAll);

        // 主类中的字段策略过滤
        SimplePropertyPreFilter simplePropertyPreFilter = FastJsonUtils.getSimplePropertyPreFilter(entity.getClass(), excludeColumn);
        Optional.ofNullable(simplePropertyPreFilter).ifPresent(serializeFilters::add);

        return JSON.toJSONString(entity, serializeFilters.toArray(new SerializeFilter[serializeFilters.size()]), SerializerFeature.WriteMapNullValue);
    }

    /**
     * 构建更新文档的json
     *
     * @param updateWrapper 条件
     * @return json
     */
    private String buildJsonDoc(Wrapper<T> updateWrapper) {
        List<EsUpdateParam> updateParamList = updateWrapper.updateParamList;
        JSONObject jsonObject = new JSONObject();

        updateParamList.forEach(esUpdateParam -> {
            String realField = FieldUtils.getRealFieldNotConvertId(esUpdateParam.getField(),
                    EntityInfoHelper.getEntityInfo(entityClass).getMappingColumnMap(),
                    GlobalConfigCache.getGlobalConfig().getDbConfig().isMapUnderscoreToCamelCase());
            jsonObject.put(realField, esUpdateParam.getValue());
        });
        return JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 执行bulk请求,并返回成功个数
     *
     * @param bulkRequest    批量请求参数
     * @param requestOptions 类型
     * @return 成功个数
     */
    private int doBulkRequest(BulkRequest bulkRequest, RequestOptions requestOptions) {
        int totalSuccess = 0;
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, requestOptions);
            if (bulkResponse.hasFailures()) {
                LogUtils.error(bulkResponse.buildFailureMessage());
            }

            Iterator<BulkItemResponse> iterator = bulkResponse.iterator();
            while (iterator.hasNext()) {
                if (Objects.equals(iterator.next().status(), RestStatus.OK)) {
                    totalSuccess++;
                }
            }
        } catch (IOException e) {
            LogUtils.error("bulk request exception", JSON.toJSONString(e));
        }
        return totalSuccess;
    }

    /**
     * 执行bulk创建请求,并返回成功个数,封装id
     *
     * @param bulkRequest    批量请求参数
     * @param requestOptions 类型
     * @param entityList     实体列表
     * @return 成功个数
     */
    private int doBulkRequest(BulkRequest bulkRequest, RequestOptions requestOptions, Collection<T> entityList) {
        int totalSuccess = 0;
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, requestOptions);
            if (bulkResponse.hasFailures()) {
                LogUtils.error(bulkResponse.buildFailureMessage());
            }

            Iterator<BulkItemResponse> iterator = bulkResponse.iterator();
            while (iterator.hasNext()) {
                BulkItemResponse next = iterator.next();
                if (Objects.equals(next.status(), RestStatus.CREATED)) {
                    setId((T) entityList.toArray()[totalSuccess], next.getId());
                    totalSuccess++;
                }
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("bulkRequest exception", e);
        }
        return totalSuccess;
    }

    /**
     * 从ES返回结果中解析出SearchHit[]
     *
     * @param searchResponse es返回的响应体
     * @return 响应体中的Hit列表
     */
    private SearchHit[] parseSearchHitArray(SearchResponse searchResponse) {
        return Optional.ofNullable(searchResponse)
                .map(SearchResponse::getHits)
                .map(SearchHits::getHits)
                .orElseThrow(() -> ExceptionUtils.eee("parseSearchHitArray exception"));
    }

    /**
     * 获取兜底索引名称
     *
     * @return 索引名称
     */
    private String getIndexName(String indexName) {
        // 优先按wrapper中指定的索引名,若未指定则取当前全局激活的索引名
        if (StringUtils.isBlank(indexName)) {
            return EntityInfoHelper.getEntityInfo(entityClass).getIndexName();
        }
        return indexName;
    }

    /**
     * 获取兜底索引名称数组
     *
     * @param indexNames 原始索引名称数组
     * @return 目标索引名称数组
     */
    private String[] getIndexNames(String... indexNames) {
        // 碰到傻狍子用户锤子索引都没指定, 给个兜底
        if (ArrayUtils.isEmpty(indexNames)) {
            return new String[]{EntityInfoHelper.getEntityInfo(entityClass).getIndexName()};
        }

        // 指定了个空字符串之类的,需要给兜底
        return Arrays.stream(indexNames)
                .map(this::getIndexName)
                .toArray(String[]::new);
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
    private void setHighlightValue(T entity, String highlightField, String value) {
        try {
            Method invokeMethod = BaseCache.setterMethod(entityClass, highlightField);
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
     * 根据全局配置决定是否控制台打印CountDSL语句
     *
     * @param countRequest 统计数量查询参数
     */
    private void printCountDSL(CountRequest countRequest) {
        if (GlobalConfigCache.getGlobalConfig().isPrintDsl() && Objects.nonNull(countRequest)) {
            Optional.ofNullable(countRequest.query())
                    .ifPresent(source -> LogUtils.info(BaseEsConstants.COUNT_DSL_PREFIX
                            + "\nindex-name: " + org.springframework.util.StringUtils.arrayToCommaDelimitedString(countRequest.indices())
                            + "\nDSL：" + source));
        }
    }

    /**
     * 根据全局配置决定是否控制台打印DSL语句
     *
     * @param searchRequest es查询请求参数
     */
    private void printDSL(SearchRequest searchRequest) {
        if (GlobalConfigCache.getGlobalConfig().isPrintDsl() && Objects.nonNull(searchRequest)) {
            Optional.ofNullable(searchRequest.source())
                    .ifPresent(source -> LogUtils.info(BaseEsConstants.DSL_PREFIX
                            + "\nindex-name: " + org.springframework.util.StringUtils.arrayToCommaDelimitedString(searchRequest.indices())
                            + "\nDSL：" + source));
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
    private void printResponseErrors(SearchResponse searchResponse) {
        if (Objects.nonNull(searchResponse)
                && searchResponse.getShardFailures() != null
                && searchResponse.getShardFailures().length > ZERO) {
            String errorMsg = searchResponse.getShardFailures()[0].toString();
            throw ExceptionUtils.eee("es响应出错，search response failed ,failedShards: " + errorMsg);
        }
    }

    /**
     * 获取刷新策略
     *
     * @return 刷新策略
     */
    private String getRefreshPolicy() {
        return GlobalConfigCache.getGlobalConfig().getDbConfig().getRefreshPolicy().getValue();
    }

    /**
     * 获取路由
     *
     * @return 路由
     */
    private String getRouting() {
        return EntityInfoHelper.getEntityInfo(entityClass).getRouting();
    }

    /**
     * 父子文档获取路由
     *
     * @param entity         实体对象
     * @param joinFieldClass join字段类
     * @return 路由
     */
    private String getRouting(T entity, Class<?> joinFieldClass) {
        // 父子类型-子文档,设置路由
        Method getJoinFieldMethod = BaseCache.getterMethod(entityClass, joinFieldClass.getSimpleName());
        Method getParentMethod = BaseCache.getterMethod(joinFieldClass, PARENT);
        try {
            Object joinField = getJoinFieldMethod.invoke(entity);
            Object parent = getParentMethod.invoke(joinField);
            return parent.toString();
        } catch (Throwable e) {
            LogUtils.formatError("build IndexRequest: child routing invoke error, joinFieldClass:%s,entity:%s,e:%s",
                    joinFieldClass.toString(), entity.toString(), e.toString());
            throw ExceptionUtils.eee("getRouting error", e);
        }
    }

}
