package com.xpc.easyes.core.conditions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.xpc.easyes.core.cache.BaseCache;
import com.xpc.easyes.core.common.EntityFieldInfo;
import com.xpc.easyes.core.common.EntityInfo;
import com.xpc.easyes.core.common.PageInfo;
import com.xpc.easyes.core.conditions.interfaces.BaseEsMapper;
import com.xpc.easyes.core.constants.BaseEsConstants;
import com.xpc.easyes.core.enums.FieldStrategy;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.core.enums.IdType;
import com.xpc.easyes.core.params.EsIndexParam;
import com.xpc.easyes.core.params.EsUpdateParam;
import com.xpc.easyes.core.toolkit.*;
import lombok.Setter;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.xpc.easyes.core.conditions.WrapperProcessor.buildSearchSourceBuilder;
import static com.xpc.easyes.core.conditions.WrapperProcessor.initBoolQueryBuilder;

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
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("existIndex exception", e);
        }
    }

    @Override
    public Boolean createIndex(LambdaEsIndexWrapper<T> wrapper) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(wrapper.indexName);

        // 分片个副本信息
        Settings.Builder settings = Settings.builder();
        Optional.ofNullable(wrapper.shardsNum).ifPresent(shards -> settings.put(BaseEsConstants.SHARDS_FIELD, shards));
        Optional.ofNullable(wrapper.replicasNum).ifPresent(replicas -> settings.put(BaseEsConstants.REPLICAS_FIELD, replicas));
        createIndexRequest.settings(settings);

        // mapping信息
        if (Objects.isNull(wrapper.mapping)) {
            List<EsIndexParam> indexParamList = wrapper.esIndexParamList;
            if (!CollectionUtils.isEmpty(indexParamList)) {
                Map<String, Object> mapping = initMapping(indexParamList);
                createIndexRequest.mapping(mapping);
            }
        } else {
            // 用户手动指定的mapping
            createIndexRequest.mapping(wrapper.mapping);
        }

        // 别名信息
        Optional.ofNullable(wrapper.aliasName).ifPresent(aliasName -> {
            Alias alias = new Alias(aliasName);
            createIndexRequest.alias(alias);
        });
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
            throw ExceptionUtils.eee("create index exception", e, wrapper.indexName);
        }
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
            Map<String, Object> mapping = initMapping(wrapper.esIndexParamList);
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
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        try {
            AcknowledgedResponse response = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw ExceptionUtils.eee("delete index exception, indexName:%s", e, indexName);
        }
    }

    @Override
    public SearchResponse search(LambdaEsQueryWrapper<T> wrapper) throws IOException {
        // 构建es restHighLevel 查询参数
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(wrapper);
        searchRequest.source(searchSourceBuilder);
        // 执行查询
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest, RequestOptions requestOptions) throws IOException {
        return client.search(searchRequest, requestOptions);
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(LambdaEsQueryWrapper<T> wrapper) {
        return buildSearchSourceBuilder(wrapper);
    }

    @Override
    public String getSource(LambdaEsQueryWrapper<T> wrapper) {
        // 获取由本框架生成的es查询参数 用于验证生成语法的正确性
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(wrapper);
        searchRequest.source(searchSourceBuilder);
        return Optional.ofNullable(searchRequest.source())
                .map(SearchSourceBuilder::toString)
                .orElseThrow(() -> ExceptionUtils.eee("get search source exception"));
    }

    @Override
    public PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper) throws IOException {
        return this.pageQueryOriginal(wrapper, BaseEsConstants.PAGE_NUM, BaseEsConstants.PAGE_SIZE);
    }

    @Override
    public PageInfo<SearchHit> pageQueryOriginal(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) throws IOException {
        PageInfo<SearchHit> pageInfo = new PageInfo<>();
        long total = this.selectCount(wrapper);
        if (total <= BaseEsConstants.ZERO) {
            return pageInfo;
        }

        // 查询数据
        SearchHit[] searchHitArray = getSearchHitArray(wrapper, pageNum, pageSize);
        List<SearchHit> list = Arrays.stream(searchHitArray).collect(Collectors.toList());
        pageInfo.setList(list);
        pageInfo.setSize(list.size());
        pageInfo.setTotal(total);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        return pageInfo;
    }

    @Override
    public PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper) {
        return initPageInfo(wrapper, BaseEsConstants.PAGE_NUM, BaseEsConstants.PAGE_SIZE);
    }

    @Override
    public PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) {
        return initPageInfo(wrapper, pageNum, pageSize);
    }


    @Override
    public Long selectCount(LambdaEsQueryWrapper<T> wrapper) {
        CountRequest countRequest = new CountRequest(getIndexName());
        BoolQueryBuilder boolQueryBuilder = initBoolQueryBuilder(wrapper.baseEsParamList);
        countRequest.query(boolQueryBuilder);
        CountResponse count;
        try {
            count = client.count(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("selectCount exception", e);
        }
        return Optional.ofNullable(count)
                .map(CountResponse::getCount)
                .orElseThrow(() -> ExceptionUtils.eee("get long count exception"));
    }

    @Override
    public Integer insert(T entity) {
        // 构建请求入参
        IndexRequest indexRequest = buildIndexRequest(entity);
        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            if (Objects.equals(indexResponse.status(), RestStatus.CREATED)) {
                setId(entity, indexResponse.getId());
                return BaseEsConstants.ONE;
            } else {
                throw ExceptionUtils.eee("insert failed, result:%s entity:%s", indexResponse.getResult(), entity);
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("insert exception:%s entity:%s", e, entity);
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
                return BaseEsConstants.ONE;
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
        list.forEach(t -> {
            try {
                Method getId = t.getClass().getMethod(BaseEsConstants.GET_ID_FUNC);
                Object invoke = getId.invoke(t);
                if (Objects.nonNull(invoke) && invoke instanceof String) {
                    String id = (String) invoke;
                    if (!StringUtils.isEmpty(id)) {
                        DeleteRequest deleteRequest = new DeleteRequest();
                        deleteRequest.id(id);
                        deleteRequest.index(getIndexName());
                        bulkRequest.add(deleteRequest);
                    }
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
        String realIdField = getRealIdFieldName();
        if (StringUtils.isEmpty(realIdField)) {
            throw ExceptionUtils.eee("the entity id not found, please check your entity:%s", entityClass.getSimpleName());
        }

        // 构建更新请求参数
        UpdateRequest updateRequest = buildUpdateRequest(entity, realIdField);
        try {
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            if (Objects.equals(updateResponse.status(), RestStatus.OK)) {
                return BaseEsConstants.ONE;
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

        // 获取实体对象中的ID字段名称
        String realIdField = getRealIdFieldName();
        if (StringUtils.isEmpty(realIdField)) {
            throw ExceptionUtils.eee("the entity id not found, please check your entity:%s", entityClass.getSimpleName());
        }

        // 封装批量请求参数
        BulkRequest bulkRequest = new BulkRequest();
        entityList.forEach(entity -> {
            UpdateRequest updateRequest = buildUpdateRequest(entity, realIdField);
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
        BoolQueryBuilder boolQueryBuilder = initBoolQueryBuilder(updateWrapper.baseEsParamList);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        // 查询id列表
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
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.termQuery(getIdFieldName(), id));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            return parseResult(searchResponse);
        } catch (Exception e) {
            throw ExceptionUtils.eee("selectById exception,id:%s", e, id);
        }
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            throw ExceptionUtils.eee("id collection must not be null or empty");
        }
        List<String> stringIdList = idList.stream().map(Object::toString).collect(Collectors.toList());
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termsQuery(getIdFieldName(), stringIdList));
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            return parseResultList(searchResponse);
        } catch (IOException e) {
            throw ExceptionUtils.eee("selectBatchIds exception,idList:%s", e, idList);
        }
    }

    @Override
    public T selectOne(LambdaEsQueryWrapper<T> wrapper) {
        long count = this.selectCount(wrapper);
        if (count > BaseEsConstants.ONE) {
            throw ExceptionUtils.eee("fond more than one result: %d", count);
        }
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(wrapper);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            return parseResult(response, wrapper);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public List<T> selectList(LambdaEsQueryWrapper<T> wrapper) {
        SearchRequest searchRequest = new SearchRequest(getIndexName());
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(wrapper);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            return parseResultList(response, wrapper);
        } catch (Exception e) {
            throw ExceptionUtils.eee("selectList exception", e);
        }
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
            SearchHit[] searchHits = parseSearchHit(searchResponse);
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
            if (IdType.NONE.equals(entityInfo.getIdType())) {
                indexRequest.id(entityInfo.getId());
            } else if (IdType.UUID.equals(entityInfo.getIdType())) {
                indexRequest.id(UUID.randomUUID().toString());
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
     * @param entity      实体
     * @param realIdField id实际字段值
     * @return 更新请求参数
     */
    private UpdateRequest buildUpdateRequest(T entity, String realIdField) {
        UpdateRequest updateRequest = new UpdateRequest();
        String getFunctionName = FieldUtils.generateGetFunctionName(realIdField);
        try {
            Method getMethod = entity.getClass().getDeclaredMethod(getFunctionName);
            Object invoke = getMethod.invoke(entity);
            if (Objects.isNull(invoke)) {
                throw ExceptionUtils.eee("unknown id value please check");
            }
            updateRequest.id(invoke.toString());
            updateRequest.index(getIndexName());
            String jsonData = buildJsonIndexSource(entity);
            updateRequest.doc(jsonData, XContentType.JSON);
        } catch (ReflectiveOperationException e) {
            throw ExceptionUtils.eee("invoke entity id value exception", e);
        }
        return updateRequest;
    }


    /**
     * 初始化分页数据
     *
     * @param wrapper  条件
     * @param pageNum  当前页
     * @param pageSize 每页条数
     * @return 分页数据
     */
    private PageInfo<T> initPageInfo(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) {
        PageInfo<T> pageInfo = new PageInfo<>();
        long total = this.selectCount(wrapper);
        if (total <= 0) {
            return pageInfo;
        }

        SearchHit[] searchHits;
        try {
            searchHits = getSearchHitArray(wrapper, pageNum, pageSize);
        } catch (IOException e) {
            throw ExceptionUtils.eee("page select exception:%s", e);
        }

        List<T> list = hitsToArray(searchHits, wrapper);
        pageInfo.setList(list);
        pageInfo.setSize(list.size());
        pageInfo.setTotal(total);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        return pageInfo;
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
        Map<String, FieldStrategy> columnMap = EntityInfoHelper
                .getEntityInfo(entityClass)
                .getFieldList()
                .stream()
                .collect(Collectors.toMap(EntityFieldInfo::getColumn, EntityFieldInfo::getFieldStrategy));

        // 根据字段配置的策略 决定是否加入到实际es处理字段中
        Set<String> goodColumn = new HashSet<>(columnMap.size());
        columnMap.forEach((fieldName, fieldStrategy) -> {
            Method invokeMethod = BaseCache.getEsEntityInvokeMethod(entityClass, fieldName);
            Object invoke;
            try {
                if (FieldStrategy.IGNORED.equals(fieldStrategy) || FieldStrategy.DEFAULT.equals(fieldStrategy)) {
                    // 忽略及无字段配置, 无全局配置 默认加入Json
                    goodColumn.add(fieldName);
                } else if (FieldStrategy.NOT_NULL.equals(fieldStrategy)) {
                    invoke = invokeMethod.invoke(entity);
                    if (Objects.nonNull(invoke)) {
                        goodColumn.add(fieldName);
                    }
                } else if (FieldStrategy.NOT_EMPTY.equals(fieldStrategy)) {
                    invoke = invokeMethod.invoke(entity);
                    if (Objects.nonNull(invoke) && invoke instanceof String) {
                        String value = (String) invoke;
                        if (!StringUtils.isEmpty(value)) {
                            goodColumn.add(fieldName);
                        }
                    }
                }
            } catch (Exception e) {
                throw ExceptionUtils.eee("buildJsonIndexSource exception, entity:%s", e, entity);
            }
        });

        SimplePropertyPreFilter simplePropertyPreFilter = getSimplePropertyPreFilter(entity.getClass(), goodColumn);
        return JSON.toJSONString(entity, simplePropertyPreFilter, SerializerFeature.WriteMapNullValue);
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
     * 初始化索引mapping
     *
     * @param indexParamList 索引参数列表
     * @return 索引mapping
     */
    private Map<String, Object> initMapping(List<EsIndexParam> indexParamList) {
        Map<String, Object> mapping = new HashMap<>(1);
        Map<String, Object> properties = new HashMap<>(indexParamList.size());
        indexParamList.forEach(indexParam -> {
            Map<String, Object> info = new HashMap<>();
            info.put(BaseEsConstants.TYPE, indexParam.getFieldType());
            // 设置分词器
            if (FieldType.TEXT.getType().equals(indexParam.getFieldType())) {
                Optional.ofNullable(indexParam.getAnalyzer())
                        .ifPresent(analyzer ->
                                info.put(BaseEsConstants.ANALYZER, indexParam.getAnalyzer().toString().toLowerCase()));
                Optional.ofNullable(indexParam.getSearchAnalyzer())
                        .ifPresent(searchAnalyzer ->
                                info.put(BaseEsConstants.SEARCH_ANALYZER, indexParam.getSearchAnalyzer().toString().toLowerCase()));
            }
            properties.put(indexParam.getFieldName(), info);
        });
        mapping.put(BaseEsConstants.PROPERTIES, properties);
        return mapping;
    }

    /**
     * 从es获取到的数据中解析出对应类型的数组 默认设置id
     *
     * @param searchResponse es返回的响应体
     * @return 指定的返回类型数据列表
     */
    private List<T> parseResultList(SearchResponse searchResponse) {
        SearchHit[] searchHits = parseSearchHit(searchResponse);
        if (ArrayUtils.isEmpty(searchHits)) {
            return new ArrayList<>(0);
        }
        return Arrays.stream(searchHits)
                .map(hit -> {
                    T entity = JSON.parseObject(hit.getSourceAsString(), entityClass);
                    setId(entity, hit.getId());
                    return entity;
                }).collect(Collectors.toList());
    }

    /**
     * 从es获取到的数据中解析出对应类型的数组 id根据查询/不查询条件决定是否设置
     *
     * @param searchResponse es返回的响应体
     * @param wrapper        条件
     * @return 指定的返回类型数据列表
     */
    private List<T> parseResultList(SearchResponse searchResponse, LambdaEsQueryWrapper<T> wrapper) {
        SearchHit[] searchHits = parseSearchHit(searchResponse);
        if (ArrayUtils.isEmpty(searchHits)) {
            return new ArrayList<>(0);
        }
        return hitsToArray(searchHits, wrapper);
    }

    /**
     * 将es返回结果集解析为数组
     *
     * @param searchHits es返回结果集
     * @param wrapper    条件
     * @return
     */
    private List<T> hitsToArray(SearchHit[] searchHits, LambdaEsQueryWrapper<T> wrapper) {
        return Arrays.stream(searchHits)
                .map(hit -> {
                    T entity = JSON.parseObject(hit.getSourceAsString(), entityClass);
                    boolean includeId = WrapperProcessor.includeId(getRealIdFieldName(), wrapper);
                    if (includeId) {
                        setId(entity, hit.getId());
                    }
                    return entity;
                }).collect(Collectors.toList());
    }

    /**
     * 从es获取到的数据中解析出对应的对象 默认设置id
     *
     * @param searchResponse es返回的响应体
     * @return 指定的返回类型数据
     */
    private T parseResult(SearchResponse searchResponse) {
        SearchHit[] searchHits = parseSearchHit(searchResponse);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }
        T entity = JSON.parseObject(searchHits[0].getSourceAsString(), entityClass);
        setId(entity, searchHits[0].getId());
        return entity;
    }

    /**
     * 从es获取到的数据中解析出对应的对象 id根据查询/不查询条件决定是否设置
     *
     * @param searchResponse es返回的响应体
     * @param wrapper        条件
     * @return 指定的返回类型数据
     */
    private T parseResult(SearchResponse searchResponse, LambdaEsQueryWrapper<T> wrapper) {
        SearchHit[] searchHits = parseSearchHit(searchResponse);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }
        T entity = JSON.parseObject(searchHits[0].getSourceAsString(), entityClass);
        boolean includeId = WrapperProcessor.includeId(getRealIdFieldName(), wrapper);
        if (includeId) {
            setId(entity, searchHits[0].getId());
        }
        return entity;
    }

    /**
     * 从ES返回结果中解析出SearchHit[]
     *
     * @param searchResponse es返回的响应体
     * @return 响应体中的Hit列表
     */
    private SearchHit[] parseSearchHit(SearchResponse searchResponse) {
        return Optional.ofNullable(searchResponse)
                .map(SearchResponse::getHits)
                .map(SearchHits::getHits)
                .orElse(new SearchHit[0]);
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
     * 设置id值
     *
     * @param entity 实体
     * @param id     主键
     */
    private void setId(T entity, String id) {
        String setMethodName = FieldUtils.generateSetFunctionName(getRealIdFieldName());
        Method invokeMethod = BaseCache.getEsEntityInvokeMethod(entityClass, setMethodName);
        try {
            invokeMethod.invoke(entity, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
