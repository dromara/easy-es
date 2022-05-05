package com.xpc.easyes.core.conditions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.xpc.easyes.core.cache.BaseCache;
import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.common.EntityFieldInfo;
import com.xpc.easyes.core.common.EntityInfo;
import com.xpc.easyes.core.common.PageInfo;
import com.xpc.easyes.core.conditions.interfaces.BaseEsMapper;
import com.xpc.easyes.core.constants.BaseEsConstants;
import com.xpc.easyes.core.enums.FieldStrategy;
import com.xpc.easyes.core.enums.IdType;
import com.xpc.easyes.core.params.CreateIndexParam;
import com.xpc.easyes.core.params.EsIndexParam;
import com.xpc.easyes.core.params.EsUpdateParam;
import com.xpc.easyes.core.toolkit.*;
import lombok.Setter;
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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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

import static com.xpc.easyes.core.conditions.WrapperProcessor.buildSearchSourceBuilder;
import static com.xpc.easyes.core.conditions.WrapperProcessor.initBoolQueryBuilder;
import static com.xpc.easyes.core.constants.BaseEsConstants.*;

/**
 * 核心 所有支持方法接口实现类
 * <p>
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
    public Boolean createIndex(LambdaEsIndexWrapper<T> wrapper) {
        // 初始化创建索引参数
        CreateIndexParam createIndexParam = new CreateIndexParam();
        createIndexParam.setIndexName(wrapper.indexName);

        // 设置分片个副本信息
        Optional.ofNullable(wrapper.shardsNum).ifPresent(createIndexParam::setShardsNum);
        Optional.ofNullable(wrapper.replicasNum).ifPresent(createIndexParam::setReplicasNum);

        // 设置mapping信息
        if (Objects.isNull(wrapper.mapping)) {
            List<EsIndexParam> indexParamList = wrapper.esIndexParamList;
            createIndexParam.setEsIndexParamList(indexParamList);
        } else {
            createIndexParam.setMapping(wrapper.mapping);
        }

        // 设置别名
        Optional.ofNullable(wrapper.aliasName).ifPresent(createIndexParam::setAliasName);

        // 创建索引
        return IndexUtils.createIndex(client, createIndexParam);
    }


    @Override
    public Boolean updateIndex(LambdaEsIndexWrapper<T> wrapper) {
        boolean existsIndex = this.existsIndex(wrapper.indexName);
        if (!existsIndex) {
            throw ExceptionUtils.eee("index: %s not exists", wrapper.indexName);
        }

        // 更新mapping
        PutMappingRequest putMappingRequest = new PutMappingRequest(wrapper.indexName);
        if (Objects.isNull(wrapper.mapping)) {
            if (CollectionUtils.isEmpty(wrapper.esIndexParamList)) {
                // 空参数列表,则不更新
                return Boolean.FALSE;
            }
            Map<String, Object> mapping = IndexUtils.initMapping(wrapper.esIndexParamList);
            putMappingRequest.source(mapping);
        } else {
            // 用户自行指定的mapping信息
            putMappingRequest.source(wrapper.mapping);
        }

        try {
            AcknowledgedResponse acknowledgedResponse = client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("update index exception", e);
        }
    }

    @Override
    public Boolean deleteIndex(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            throw ExceptionUtils.eee("indexName can not be empty");
        }
        return IndexUtils.deleteIndex(client, indexName);
    }

    @Override
    public SearchResponse search(LambdaEsQueryWrapper<T> wrapper) {
        // 构建es restHighLevel 查询参数
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        printDSL(wrapper);
        // 执行查询
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("search exception", e);
        }
        return response;
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest, RequestOptions requestOptions) throws IOException {
        printDSL(searchRequest);
        return client.search(searchRequest, requestOptions);
    }

    @Override
    public SearchResponse scroll(SearchScrollRequest searchScrollRequest, RequestOptions requestOptions) throws IOException {
        return client.scroll(searchScrollRequest, requestOptions);
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(LambdaEsQueryWrapper<T> wrapper) {
        return buildSearchSourceBuilder(wrapper, entityClass);
    }

    @Override
    public String getSource(LambdaEsQueryWrapper<T> wrapper) {
        // 获取由本框架生成的es查询参数 用于验证生成语法的正确性
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        return Optional.ofNullable(searchRequest.source())
                .map(SearchSourceBuilder::toString)
                .orElseThrow(() -> ExceptionUtils.eee("get search source exception"));
    }

    @Override
    @Deprecated
    public PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper) throws IOException {
        return this.pageQueryOriginal(wrapper, BaseEsConstants.PAGE_NUM, BaseEsConstants.PAGE_SIZE);
    }

    @Override
    public PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) throws IOException {
        long total = this.selectCount(wrapper);
        if (total <= BaseEsConstants.ZERO) {
            return new PageInfo<>();
        }

        // 查询数据
        SearchHit[] searchHitArray = getSearchHitArray(wrapper, pageNum, pageSize);
        List<SearchHit> list = Arrays.stream(searchHitArray).collect(Collectors.toList());

        return PageHelper.getPageInfo(list, total, pageNum, pageSize);
    }

    @Override
    public PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) {
        // 兼容分页参数
        pageNum = pageNum == null || pageNum <= ZERO ? BaseEsConstants.PAGE_NUM : pageNum;
        pageSize = pageSize == null || pageSize <= ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;
        wrapper.from((pageNum - 1) * pageSize);
        wrapper.size(pageSize);

        // 请求es获取数据
        SearchResponse response = getSearchResponse(wrapper);

        // 解析数据
        SearchHit[] searchHits = parseSearchHitArray(response);
        List<T> dataList = Arrays.stream(searchHits).map(this::parseOne).collect(Collectors.toList());
        long count = parseCount(response, Objects.nonNull(wrapper.distinctField));
        return PageHelper.getPageInfo(dataList, count, pageNum, pageSize);
    }

    @Override
    public Long selectCount(LambdaEsQueryWrapper<T> wrapper, boolean distinct) {
        if (distinct) {
            // 去重, 总数来源于桶
            SearchResponse response = getSearchResponse(wrapper);
            return parseCount(response, Objects.nonNull(wrapper.distinctField));
        } else {
            // 不去重,直接count获取,效率更高
            CountRequest countRequest = new CountRequest(getIndexName());
            BoolQueryBuilder boolQueryBuilder = initBoolQueryBuilder(wrapper.baseEsParamList, entityClass);
            countRequest.query(boolQueryBuilder);
            CountResponse count;
            try {
                printCountDSL(wrapper);
                count = client.count(countRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw ExceptionUtils.eee("selectCount exception", e);
            }
            return count.getCount();
        }
    }

    @Override
    public Integer insert(T entity) {
        // 构建请求入参
        IndexRequest indexRequest = buildIndexRequest(entity);
        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            if (Objects.equals(indexResponse.status(), RestStatus.CREATED)) {
                setId(entity, indexResponse.getId());
                return ONE;
            } else if (Objects.equals(indexResponse.status(), RestStatus.OK)) {
                // 该id已存在,数据被更新的情况
                return BaseEsConstants.ZERO;
            } else {
                throw ExceptionUtils.eee("insert failed, result:%s entity:%s", indexResponse.getResult(), entity);
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("insert entity:%s exception", e, entity);
        }
    }

    @Override
    public Integer insertBatch(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }
        // 构建批量请求参数
        BulkRequest bulkRequest = new BulkRequest();
        entityList.forEach(entity -> {
            IndexRequest indexRequest = buildIndexRequest(entity);
            bulkRequest.add(indexRequest);
        });
        // 执行批量请求并返回结果
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT, entityList);
    }

    @Override
    public Integer deleteById(Serializable id) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.id(id.toString());
        deleteRequest.index(getIndexName());
        try {
            DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
            if (Objects.equals(deleteResponse.status(), RestStatus.OK)) {
                return ONE;
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("deleteById exception:%s, id:%s", e, id);
        }
        return BaseEsConstants.ZERO;
    }

    @Override
    public Integer delete(LambdaEsQueryWrapper<T> wrapper) {
        List<T> list = this.selectList(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return BaseEsConstants.ZERO;
        }
        BulkRequest bulkRequest = new BulkRequest();
        Method getId = BaseCache.getterMethod(entityClass, getRealIdFieldName());
        list.forEach(t -> {
            try {
                Object id = getId.invoke(t);
                if (Objects.nonNull(id)) {
                    DeleteRequest deleteRequest = new DeleteRequest();
                    deleteRequest.id(id.toString());
                    deleteRequest.index(getIndexName());
                    bulkRequest.add(deleteRequest);

                }
            } catch (Exception e) {
                throw ExceptionUtils.eee("delete exception", e);
            }
        });
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public Integer deleteBatchIds(Collection<? extends Serializable> idList) {
        Assert.notEmpty(idList, "the collection of id must not empty");
        BulkRequest bulkRequest = new BulkRequest();
        idList.forEach(id -> {
            if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
                throw ExceptionUtils.eee("id must not be null or empty");
            }
            DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.id(id.toString());
            deleteRequest.index(getIndexName());
            bulkRequest.add(deleteRequest);
        });
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }


    @Override
    public Integer updateById(T entity) {
        // 获取id值
        String idValue = getIdValue(entityClass, entity);

        // 构建更新请求参数
        UpdateRequest updateRequest = buildUpdateRequest(entity, idValue);

        // 执行更新
        try {
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            if (Objects.equals(updateResponse.status(), RestStatus.OK)) {
                return ONE;
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("updateById exception,entity:%s", e, entity);
        }

        return BaseEsConstants.ZERO;
    }

    @Override
    public Integer updateBatchByIds(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }

        // 封装批量请求参数
        BulkRequest bulkRequest = new BulkRequest();
        entityList.forEach(entity -> {
            String idValue = getIdValue(entityClass, entity);
            UpdateRequest updateRequest = buildUpdateRequest(entity, idValue);
            bulkRequest.add(updateRequest);
        });

        // 执行批量请求
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public Integer update(T entity, LambdaEsUpdateWrapper<T> updateWrapper) {
        if (Objects.isNull(entity) && CollectionUtils.isEmpty(updateWrapper.updateParamList)) {
            return BaseEsConstants.ZERO;
        }

        // 构建查询条件
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = initBoolQueryBuilder(updateWrapper.baseEsParamList, entityClass);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        // 查询id列表
        printDSL(searchRequest);
        List<String> idList = this.selectIdList(searchRequest);
        if (CollectionUtils.isEmpty(idList)) {
            return BaseEsConstants.ZERO;
        }

        // 获取更新文档内容
        String jsonData = Optional.ofNullable(entity)
                .map(this::buildJsonIndexSource)
                .orElseGet(() -> buildJsonDoc(updateWrapper));

        // 批量更新
        BulkRequest bulkRequest = new BulkRequest();
        String index = getIndexName();
        idList.forEach(id -> {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.id(id).index(index);
            updateRequest.doc(jsonData, XContentType.JSON);
            bulkRequest.add(updateRequest);
        });
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public T selectById(Serializable id) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }

        // 构造查询参数
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(getIdFieldName(), id));
        searchRequest.source(searchSourceBuilder);

        // 请求es获取数据
        SearchHit[] searchHits = getSearchHitArray(searchRequest);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }

        // 解析数据
        return parseOne(searchHits[0]);
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            throw ExceptionUtils.eee("id collection must not be null or empty");
        }

        // 构造查询参数
        List<String> stringIdList = idList.stream().map(Object::toString).collect(Collectors.toList());
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termsQuery(getIdFieldName(), stringIdList));
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
    public T selectOne(LambdaEsQueryWrapper<T> wrapper) {
        long count = this.selectCount(wrapper);
        if (count > ONE && wrapper.size > ONE) {
            throw ExceptionUtils.eee("fond more than one result: %d , please use limit function to limit 1", count);
        }

        // 请求es获取数据
        SearchHit[] searchHits = getSearchHitArray(wrapper);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }

        // 解析首条数据
        return parseOne(searchHits[0], wrapper);
    }

    @Override
    public List<T> selectList(LambdaEsQueryWrapper<T> wrapper) {
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

    /**
     * 查询id列表
     *
     * @param searchRequest 查询参数
     * @return id列表
     */
    private List<String> selectIdList(SearchRequest searchRequest) {
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = parseSearchHitArray(searchResponse);
            return Arrays.stream(searchHits)
                    .map(SearchHit::getId)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw ExceptionUtils.eee("selectIdList exception", e);
        }
    }

    /**
     * 构建创建数据请求参数
     *
     * @param entity 实体
     * @return es请求参数
     */
    private IndexRequest buildIndexRequest(T entity) {
        IndexRequest indexRequest = new IndexRequest();

        // id预处理,除下述情况,其它情况使用es默认的id
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entity.getClass());
        if (!StringUtils.isEmpty(entityInfo.getId())) {
            if (IdType.UUID.equals(entityInfo.getIdType())) {
                indexRequest.id(UUID.randomUUID().toString());
            } else if (IdType.CUSTOMIZE.equals(entityInfo.getIdType())) {
                indexRequest.id(getIdValue(entityClass, entity));
            }
        }

        // 构建插入的json格式数据
        String jsonData = buildJsonIndexSource(entity);
        indexRequest.index(entityInfo.getIndexName());
        indexRequest.source(jsonData, XContentType.JSON);
        return indexRequest;
    }

    /**
     * 构建更新数据请求参数
     *
     * @param entity  实体
     * @param idValue id值
     * @return 更新请求参数
     */
    private UpdateRequest buildUpdateRequest(T entity, String idValue) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.id(idValue);
        updateRequest.index(getIndexName());
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
                        ParsedCardinality parsedCardinality = aggregations.get(REPEAT_NUM_KEY);
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
        T entity = JSON.parseObject(searchHit.getSourceAsString(), entityClass,
                EntityInfoHelper.getEntityInfo(entityClass).getExtraProcessor());

        setId(entity, searchHit.getId());

        return entity;
    }

    /**
     * 从searchHit中解析一条数据
     *
     * @param searchHit es返回数据
     * @param wrapper   参数包装类
     * @return 实际想要的数据
     */
    private T parseOne(SearchHit searchHit, LambdaEsQueryWrapper<T> wrapper) {
        // 解析json
        T entity = JSON.parseObject(searchHit.getSourceAsString(), entityClass,
                EntityInfoHelper.getEntityInfo(entityClass).getExtraProcessor());

        // 高亮字段处理
        if (!CollectionUtils.isEmpty(wrapper.highLightParamList)) {
            Map<String, String> highlightFieldMap = getHighlightFieldMap();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            highlightFields.forEach((key, value) -> {
                String highLightValue = Arrays.stream(value.getFragments()).findFirst().map(Text::string).orElse(EMPTY_STR);
                setHighlightValue(entity, highlightFieldMap.get(key), highLightValue);
            });
        }

        // id处理
        boolean includeId = WrapperProcessor.includeId(getRealIdFieldName(), wrapper);
        if (includeId) {
            setId(entity, searchHit.getId());
        }

        return entity;
    }

    /**
     * 获取es搜索响应体
     *
     * @param wrapper 条件
     * @return 搜索响应体
     */
    private SearchResponse getSearchResponse(LambdaEsQueryWrapper<T> wrapper) {
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
        SearchResponse searchResponse;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray exception,searchRequest:%s", e, searchRequest);
        }
        return parseSearchHitArray(searchResponse);
    }


    /**
     * 从es中请求获取searchHit数组
     *
     * @param wrapper 参数包装类
     * @return searchHit数组
     */
    private SearchHit[] getSearchHitArray(LambdaEsQueryWrapper<T> wrapper) {
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        printDSL(wrapper);
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray IOException, searchRequest:%s", e, searchRequest);
        }
        return parseSearchHitArray(response);
    }

    /**
     * 获取查询结果数组
     *
     * @param wrapper  条件
     * @param pageNum  当前页
     * @param pageSize 每页条数
     * @return es返回结果体
     * @throws IOException IO异常
     */
    private SearchHit[] getSearchHitArray(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) throws IOException {
        wrapper.from((pageNum - 1) * pageSize);
        wrapper.size(pageSize);
        SearchResponse response = search(wrapper);
        return Optional.ofNullable(response)
                .map(SearchResponse::getHits)
                .map(SearchHits::getHits)
                .orElseThrow(() -> ExceptionUtils.eee("get searchHits exception,the response from es is null"));
    }


    /**
     * 构建,插入/更新 的JSON对象
     *
     * @param entity 实体
     * @return json
     */
    private String buildJsonIndexSource(T entity) {
        // 获取所有字段列表
        Class<?> entityClass = entity.getClass();

        // 根据字段配置的策略 决定是否加入到实际es处理字段中
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        List<EntityFieldInfo> fieldList = entityInfo.getFieldList();
        Set<String> goodColumn = new HashSet<>(fieldList.size());

        fieldList.forEach(field -> {
            String column = field.getColumn();
            Method invokeMethod = BaseCache.getterMethod(entityClass, column);
            Object invoke;
            FieldStrategy fieldStrategy = field.getFieldStrategy();
            try {
                if (FieldStrategy.IGNORED.equals(fieldStrategy) || FieldStrategy.DEFAULT.equals(fieldStrategy)) {
                    // 忽略及无字段配置, 无全局配置 默认加入Json
                    goodColumn.add(column);
                } else if (FieldStrategy.NOT_NULL.equals(fieldStrategy)) {
                    invoke = invokeMethod.invoke(entity);
                    if (Objects.nonNull(invoke)) {
                        goodColumn.add(column);
                    }
                } else if (FieldStrategy.NOT_EMPTY.equals(fieldStrategy)) {
                    invoke = invokeMethod.invoke(entity);
                    Optional.ofNullable(invoke)
                            .ifPresent(value -> {
                                if (value instanceof String) {
                                    String strValue = (String) invoke;
                                    if (!StringUtils.isEmpty(strValue)) {
                                        goodColumn.add(column);
                                    }
                                } else {
                                    goodColumn.add(column);
                                }
                            });
                }
            } catch (Exception e) {
                throw ExceptionUtils.eee("buildJsonIndexSource exception, entity:%s", e, entity);
            }
        });

        SimplePropertyPreFilter simplePropertyPreFilter = getSimplePropertyPreFilter(entity.getClass(), goodColumn);
        SerializeFilter[] filters = {simplePropertyPreFilter, entityInfo.getSerializeFilter()};
        return JSON.toJSONString(entity, filters, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 构建更新文档的json
     *
     * @param updateWrapper 条件
     * @return json
     */
    private String buildJsonDoc(LambdaEsUpdateWrapper<T> updateWrapper) {
        List<EsUpdateParam> updateParamList = updateWrapper.updateParamList;
        JSONObject jsonObject = new JSONObject();
        updateParamList.forEach(esUpdateParam -> jsonObject.put(esUpdateParam.getField(), esUpdateParam.getValue()));
        return JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 设置fastjson toJsonString字段
     *
     * @param clazz  类
     * @param fields 字段列表
     * @return
     */
    private SimplePropertyPreFilter getSimplePropertyPreFilter(Class<?> clazz, Set<String> fields) {
        return new SimplePropertyPreFilter(clazz, fields.toArray(new String[fields.size()]));
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
            Iterator<BulkItemResponse> iterator = bulkResponse.iterator();
            while (iterator.hasNext()) {
                if (Objects.equals(iterator.next().status(), RestStatus.OK)) {
                    totalSuccess++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
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
                throw ExceptionUtils.eee("bulkRequest has failures");
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
            throw ExceptionUtils.eee("bulkRequest exception, msg:%s,cause:%s", e.getMessage(), e.getCause());
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
                .orElseThrow(() -> ExceptionUtils.eee("parseSearchHitArray exception, response:%s", searchResponse));
    }

    /**
     * 获取索引名称
     *
     * @return 索引名称
     */
    private String getIndexName() {
        return EntityInfoHelper.getEntityInfo(entityClass).getIndexName();
    }

    /**
     * 获取id字段名称(注解中的)
     *
     * @return id字段名称
     */
    private String getIdFieldName() {
        return EntityInfoHelper.getEntityInfo(entityClass).getKeyColumn();
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
            e.printStackTrace();
        }
    }

    /**
     * 设置id值
     *
     * @param entity 实体
     * @param id     主键
     */
    private void setId(T entity, String id) {
        try {
            Method invokeMethod = BaseCache.setterMethod(entityClass, getRealIdFieldName());

            // 将es返回的String类型id还原为字段实际的id类型,比如Long,否则反射会报错
            Class<?> idClass = EntityInfoHelper.getEntityInfo(entityClass).getIdClass();
            Object val = ReflectionKit.getVal(id, idClass);
            invokeMethod.invoke(entity, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取实体对象的id值
     *
     * @param entityClass 实体类
     * @param entity      实体对象
     * @return id值
     */
    private String getIdValue(Class<T> entityClass, T entity) {
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
     * 根据全局配置决定是否控制台打印DSL语句
     *
     * @param wrapper
     */
    private void printDSL(LambdaEsQueryWrapper<T> wrapper) {
        if (GlobalConfigCache.getGlobalConfig().isPrintDsl()) {
            LogUtils.info(DSL_PREFIX + getSource(wrapper));
        }

    }

    /**
     * 根据全局配置决定是否控制台打印CountDSL语句
     *
     * @param wrapper 查询参数包装类
     */
    private void printCountDSL(LambdaEsQueryWrapper<T> wrapper) {
        if (GlobalConfigCache.getGlobalConfig().isPrintDsl()) {
            LogUtils.info(COUNT_DSL_PREFIX + getSource(wrapper));
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
                    .ifPresent(source -> LogUtils.info(DSL_PREFIX + source));
        }
    }

}
