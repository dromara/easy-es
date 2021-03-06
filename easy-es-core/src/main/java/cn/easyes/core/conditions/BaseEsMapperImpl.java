package cn.easyes.core.conditions;

import cn.easyes.common.constants.BaseEsConstants;
import cn.easyes.common.enums.FieldStrategy;
import cn.easyes.common.enums.IdType;
import cn.easyes.common.utils.*;
import cn.easyes.core.biz.*;
import cn.easyes.core.cache.BaseCache;
import cn.easyes.core.cache.GlobalConfigCache;
import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.easyes.core.toolkit.EntityInfoHelper;
import cn.easyes.core.toolkit.FieldUtils;
import cn.easyes.core.toolkit.IndexUtils;
import cn.easyes.core.toolkit.PageHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
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
import org.elasticsearch.client.indices.GetIndexResponse;
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

import static cn.easyes.common.constants.BaseEsConstants.*;

/**
 * ?????? ?????????????????????????????????
 * <p>
 * Copyright ?? 2021 xpc1024 All Rights Reserved
 **/
public class BaseEsMapperImpl<T> implements BaseEsMapper<T> {
    /**
     * restHighLevel client
     */
    @Setter
    private RestHighLevelClient client;
    /**
     * T ????????????
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
    public Boolean createIndex(LambdaEsIndexWrapper<T> wrapper) {
        // ???????????????????????????
        CreateIndexParam createIndexParam = new CreateIndexParam();
        createIndexParam.setIndexName(wrapper.indexName);

        // ???????????????????????????
        Optional.ofNullable(wrapper.shardsNum).ifPresent(createIndexParam::setShardsNum);
        Optional.ofNullable(wrapper.replicasNum).ifPresent(createIndexParam::setReplicasNum);

        // ????????????????????????settings
        Optional.ofNullable(wrapper.settings).ifPresent(createIndexParam::setSettings);

        // ??????wrapper?????????mapping????????????
        List<EsIndexParam> indexParamList = wrapper.esIndexParamList;
        createIndexParam.setEsIndexParamList(indexParamList);

        // ????????????????????????mapping??????
        Optional.ofNullable(wrapper.mapping).ifPresent(createIndexParam::setMapping);

        // ????????????
        Optional.ofNullable(wrapper.aliasName).ifPresent(createIndexParam::setAliasName);

        // ????????????
        return IndexUtils.createIndex(client, EntityInfoHelper.getEntityInfo(entityClass), createIndexParam);
    }


    @Override
    public Boolean updateIndex(LambdaEsIndexWrapper<T> wrapper) {
        boolean existsIndex = this.existsIndex(wrapper.indexName);
        if (!existsIndex) {
            throw ExceptionUtils.eee("index: %s not exists", wrapper.indexName);
        }

        // ??????mapping
        PutMappingRequest putMappingRequest = new PutMappingRequest(wrapper.indexName);
        if (Objects.isNull(wrapper.mapping)) {
            if (CollectionUtils.isEmpty(wrapper.esIndexParamList)) {
                // ???????????????,????????????
                return Boolean.FALSE;
            }
            Map<String, Object> mapping = IndexUtils.initMapping(EntityInfoHelper.getEntityInfo(entityClass), wrapper.esIndexParamList);
            putMappingRequest.source(mapping);
        } else {
            // ?????????????????????mapping??????
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
        // ??????es restHighLevel ????????????
        SearchRequest searchRequest = new SearchRequest(getIndexName(wrapper.indexName));
        SearchSourceBuilder searchSourceBuilder = WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        printDSL(searchRequest);
        // ????????????
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
        return WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
    }

    @Override
    public String getSource(LambdaEsQueryWrapper<T> wrapper) {
        // ???????????????????????????es???????????? ????????????????????????????????????
        SearchRequest searchRequest = new SearchRequest(getIndexName(wrapper.indexName));
        SearchSourceBuilder searchSourceBuilder = WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        return Optional.ofNullable(searchRequest.source())
                .map(SearchSourceBuilder::toString)
                .orElseThrow(() -> ExceptionUtils.eee("get search source exception"));
    }

    @Override
    public PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) {
        // ??????????????????
        pageNum = pageNum == null || pageNum <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_NUM : pageNum;
        pageSize = pageSize == null || pageSize <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;

        wrapper.from((pageNum - 1) * pageSize);
        wrapper.size(pageSize);

        // ??????es????????????
        SearchResponse response = getSearchResponse(wrapper);

        // ????????????
        SearchHit[] searchHits = parseSearchHitArray(response);
        List<T> dataList = Arrays.stream(searchHits).map(searchHit -> parseOne(searchHit, wrapper))
                .collect(Collectors.toList());
        long count = parseCount(response, Objects.nonNull(wrapper.distinctField));
        return PageHelper.getPageInfo(dataList, count, pageNum, pageSize);
    }

    @Override
    public Long selectCount(LambdaEsQueryWrapper<T> wrapper, boolean distinct) {
        // ??????id???,????????????
        wrapper.select(DEFAULT_ES_ID_NAME);

        if (distinct) {
            // ??????, ??????????????????
            SearchResponse response = getSearchResponse(wrapper);
            return parseCount(response, Objects.nonNull(wrapper.distinctField));
        } else {
            // ?????????,??????count??????,????????????
            CountRequest countRequest = new CountRequest(getIndexName(wrapper.indexName));
            BoolQueryBuilder boolQueryBuilder = WrapperProcessor.initBoolQueryBuilder(wrapper.baseEsParamList,
                    wrapper.enableMust2Filter, entityClass);
            countRequest.query(boolQueryBuilder);
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
    public Integer insert(T entity, String indexName) {
        Assert.notNull(entity, "insert entity must not be null");

        // ??????????????????
        IndexRequest indexRequest = buildIndexRequest(entity, indexName);
        indexRequest.setRefreshPolicy(getRefreshPolicy());

        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            if (Objects.equals(indexResponse.status(), RestStatus.CREATED)) {
                setId(entity, indexResponse.getId());
                return BaseEsConstants.ONE;
            } else if (Objects.equals(indexResponse.status(), RestStatus.OK)) {
                // ???id?????????,????????????????????????
                return BaseEsConstants.ZERO;
            } else {
                throw ExceptionUtils.eee("insert failed, result:%s entity:%s", indexResponse.getResult(), entity);
            }
        } catch (IOException e) {
            throw ExceptionUtils.eee("insert entity:%s exception", e, entity.toString());
        }
    }

    @Override
    public Integer insertBatch(Collection<T> entityList) {
        return insertBatch(entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insertBatch(Collection<T> entityList, String indexName) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }
        // ????????????????????????
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        entityList.forEach(entity -> {
            IndexRequest indexRequest = buildIndexRequest(entity, indexName);
            bulkRequest.add(indexRequest);
        });
        // ?????????????????????????????????
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT, entityList);
    }

    @Override
    public Integer deleteById(Serializable id) {
        return deleteById(id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteById(Serializable id, String indexName) {
        DeleteRequest deleteRequest = generateDelRequest(id, indexName);
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

    @Override
    public Integer delete(LambdaEsQueryWrapper<T> wrapper) {
        // ??????id???,????????????
        wrapper.select(EntityInfoHelper.getEntityInfo(entityClass).getKeyProperty());

        List<T> list = this.selectList(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return BaseEsConstants.ZERO;
        }

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        Method getId = BaseCache.getterMethod(entityClass, getRealIdFieldName());
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        list.forEach(item -> {
            try {
                Object id = getId.invoke(item);
                if (Objects.nonNull(id)) {
                    DeleteRequest deleteRequest = new DeleteRequest();
                    deleteRequest.id(id.toString());

                    // ????????????-?????????,???????????????,??????????????????
                    if (entityInfo.isChild()) {
                        String routing = getRouting(item, entityInfo.getJoinFieldClass());
                        deleteRequest.routing(routing);
                    }

                    deleteRequest.index(getIndexName(wrapper.indexName));
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
        return deleteBatchIds(idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer deleteBatchIds(Collection<? extends Serializable> idList, String indexName) {
        Assert.notEmpty(idList, "the collection of id must not empty");
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        idList.forEach(id -> {
            DeleteRequest deleteRequest = generateDelRequest(id, indexName);
            bulkRequest.add(deleteRequest);
        });
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public Integer updateById(T entity) {
        return updateById(entity, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateById(T entity, String indexName) {
        // ??????id???
        String idValue = getIdValue(entity);

        // ????????????????????????
        UpdateRequest updateRequest = buildUpdateRequest(entity, idValue, indexName);
        updateRequest.setRefreshPolicy(getRefreshPolicy());

        // ????????????
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

    @Override
    public Integer updateBatchByIds(Collection<T> entityList) {
        return updateBatchByIds(entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateBatchByIds(Collection<T> entityList, String indexName) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }

        // ????????????????????????
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        entityList.forEach(entity -> {
            String idValue = getIdValue(entity);
            UpdateRequest updateRequest = buildUpdateRequest(entity, idValue, indexName);
            bulkRequest.add(updateRequest);
        });

        // ??????????????????
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public Integer update(T entity, LambdaEsUpdateWrapper<T> updateWrapper) {
        if (Objects.isNull(entity) && CollectionUtils.isEmpty(updateWrapper.updateParamList)) {
            return BaseEsConstants.ZERO;
        }

        // ??????????????????
        List<T> list = selectListByUpdateWrapper(updateWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return BaseEsConstants.ZERO;
        }

        // ????????????????????????
        String jsonData = Optional.ofNullable(entity)
                .map(this::buildJsonIndexSource)
                .orElseGet(() -> buildJsonDoc(updateWrapper));

        // ????????????
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        String index = getIndexName(updateWrapper.indexName);
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
            updateRequest.index(index);
            updateRequest.doc(jsonData, XContentType.JSON);

            // ????????????-?????????,???????????????,??????????????????
            if (entityInfo.isChild()) {
                String routing = getRouting(item, entityInfo.getJoinFieldClass());
                updateRequest.routing(routing);
            }
            bulkRequest.add(updateRequest);
        });
        return doBulkRequest(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public T selectById(Serializable id) {
        return selectById(id, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public T selectById(Serializable id, String indexName) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }

        // ??????????????????
        SearchRequest searchRequest = new SearchRequest(getIndexName(indexName));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(DEFAULT_ES_ID_NAME, id));
        searchRequest.source(searchSourceBuilder);

        // ??????es????????????
        SearchHit[] searchHits = getSearchHitArray(searchRequest);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }

        // ????????????
        return parseOne(searchHits[0]);
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList) {
        return selectBatchIds(idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList, String indexName) {
        if (CollectionUtils.isEmpty(idList)) {
            throw ExceptionUtils.eee("id collection must not be null or empty");
        }

        // ??????????????????
        List<String> stringIdList = idList.stream().map(Object::toString).collect(Collectors.toList());
        SearchRequest searchRequest = new SearchRequest(getIndexName(indexName));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termsQuery(DEFAULT_ES_ID_NAME, stringIdList));
        searchRequest.source(sourceBuilder);

        // ??????es????????????
        SearchHit[] searchHitArray = getSearchHitArray(searchRequest);
        if (ArrayUtils.isEmpty(searchHitArray)) {
            return new ArrayList<>(0);
        }

        // ??????????????????
        return Arrays.stream(searchHitArray)
                .map(this::parseOne)
                .collect(Collectors.toList());
    }

    @Override
    public T selectOne(LambdaEsQueryWrapper<T> wrapper) {
        // ??????es????????????
        SearchResponse searchResponse = getSearchResponse(wrapper);
        long count = parseCount(searchResponse, Objects.nonNull(wrapper.distinctField));
        boolean invalid = (count > ONE && (Objects.nonNull(wrapper.size) && wrapper.size > ONE))
                || (count > ONE && Objects.isNull(wrapper.size));
        if (invalid) {
            LogUtils.error("found more than one result:" + count, "please use wrapper.limit to limit 1");
            throw ExceptionUtils.eee("found more than one result: %d, please use wrapper.limit to limit 1", count);
        }

        // ????????????
        SearchHit[] searchHits = parseSearchHitArray(searchResponse);
        if (ArrayUtils.isEmpty(searchHits)) {
            return null;
        }
        return parseOne(searchHits[0], wrapper);
    }

    @Override
    public List<T> selectList(LambdaEsQueryWrapper<T> wrapper) {
        // ??????es????????????
        SearchHit[] searchHits = getSearchHitArray(wrapper);
        if (ArrayUtils.isEmpty(searchHits)) {
            return new ArrayList<>(0);
        }

        // ????????????
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
     * ??????DelRequest????????????
     *
     * @param id        id
     * @param indexName ?????????
     * @return DelRequest
     */
    private DeleteRequest generateDelRequest(Serializable id, String indexName) {
        if (Objects.isNull(id) || StringUtils.isEmpty(id.toString())) {
            throw ExceptionUtils.eee("id must not be null or empty");
        }
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.id(id.toString());
        deleteRequest.index(getIndexName(indexName));
        return deleteRequest;
    }

    /**
     * ??????????????????
     *
     * @param wrapper ????????????
     * @return ????????????
     */
    private List<T> selectListByUpdateWrapper(LambdaEsUpdateWrapper<T> wrapper) {
        // ??????????????????
        SearchRequest searchRequest = new SearchRequest(getIndexName(wrapper.indexName));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ??????id???,????????????
        searchSourceBuilder.fetchSource(DEFAULT_ES_ID_NAME, null);
        BoolQueryBuilder boolQueryBuilder = WrapperProcessor.initBoolQueryBuilder(wrapper.baseEsParamList,
                wrapper.enableMust2Filter, entityClass);
        Optional.ofNullable(wrapper.geoParam).ifPresent(geoParam -> WrapperProcessor.setGeoQuery(geoParam, boolQueryBuilder, entityClass));
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        printDSL(searchRequest);
        try {
            //  ??????????????????
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = parseSearchHitArray(searchResponse);
            return Arrays.stream(searchHits)
                    .map(this::parseOne)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw ExceptionUtils.eee("selectIdList exception", e);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param entity    ??????
     * @param indexName ?????????
     * @return es????????????
     */
    private IndexRequest buildIndexRequest(T entity, String indexName) {
        IndexRequest indexRequest = new IndexRequest();

        // id?????????,???????????????,??????????????????es?????????id
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);

        if (IdType.UUID.equals(entityInfo.getIdType())) {
            indexRequest.id(UUID.randomUUID().toString());
        } else if (IdType.CUSTOMIZE.equals(entityInfo.getIdType())) {
            indexRequest.id(getIdValue(entity));
        }

        // ???????????????json????????????
        String jsonData = buildJsonIndexSource(entity);

        indexName = StringUtils.isBlank(indexName) ? entityInfo.getIndexName() : indexName;
        indexRequest.index(indexName);
        indexRequest.source(jsonData, XContentType.JSON);

        // ????????????-?????????,????????????
        if (entityInfo.isChild()) {
            String routing = getRouting(entity, entityInfo.getJoinFieldClass());
            indexRequest.routing(routing);
        }

        return indexRequest;
    }

    /**
     * ??????????????????????????????
     *
     * @param entity    ??????
     * @param idValue   id???
     * @param indexName ?????????
     * @return ??????????????????
     */
    private UpdateRequest buildUpdateRequest(T entity, String idValue, String indexName) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.id(idValue);
        updateRequest.index(getIndexName(indexName));
        String jsonData = buildJsonIndexSource(entity);
        updateRequest.doc(jsonData, XContentType.JSON);
        return updateRequest;
    }

    /**
     * ????????????????????????
     *
     * @param response es???????????????
     * @param distinct ??????????????????
     * @return ??????
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
     * ???searchHit?????????????????????
     *
     * @param searchHit es????????????
     * @return ?????????????????????
     */
    private T parseOne(SearchHit searchHit) {
        T entity = JSON.parseObject(searchHit.getSourceAsString(), entityClass,
                EntityInfoHelper.getEntityInfo(entityClass).getExtraProcessor());

        setId(entity, searchHit.getId());

        return entity;
    }

    /**
     * ???searchHit?????????????????????
     *
     * @param searchHit es????????????
     * @param wrapper   ???????????????
     * @return ?????????????????????
     */
    private T parseOne(SearchHit searchHit, LambdaEsQueryWrapper<T> wrapper) {
        EntityInfo entityInfo = EntityInfoHelper.getEntityInfo(entityClass);
        // ??????json
        T entity = JSON.parseObject(searchHit.getSourceAsString(), entityClass, entityInfo.getExtraProcessor());

        // ??????????????????
        if (CollectionUtils.isNotEmpty(entityInfo.getHighLightParams())) {
            Map<String, String> highlightFieldMap = getHighlightFieldMap();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            highlightFields.forEach((key, value) -> {
                String highLightValue = Arrays.stream(value.getFragments()).findFirst().map(Text::string).orElse(BaseEsConstants.EMPTY_STR);
                setHighlightValue(entity, highlightFieldMap.get(key), highLightValue);
            });
        }

        // id??????
        boolean includeId = WrapperProcessor.includeId(getRealIdFieldName(), wrapper);
        if (includeId) {
            setId(entity, searchHit.getId());
        }

        return entity;
    }

    /**
     * ??????es???????????????
     *
     * @param wrapper ??????
     * @return ???????????????
     */
    private SearchResponse getSearchResponse(LambdaEsQueryWrapper<T> wrapper) {
        return search(wrapper);
    }

    /**
     * ???es???????????????searchHit??????
     *
     * @param searchRequest ????????????
     * @return searchHit??????
     */
    private SearchHit[] getSearchHitArray(SearchRequest searchRequest) {
        printDSL(searchRequest);
        SearchResponse searchResponse;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray exception,searchRequest:%s", e, searchRequest.toString());
        }
        return parseSearchHitArray(searchResponse);
    }


    /**
     * ???es???????????????searchHit??????
     *
     * @param wrapper ???????????????
     * @return searchHit??????
     */
    private SearchHit[] getSearchHitArray(LambdaEsQueryWrapper<T> wrapper) {
        SearchRequest searchRequest = new SearchRequest(getIndexName(wrapper.indexName));
        SearchSourceBuilder searchSourceBuilder = WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        printDSL(searchRequest);
        SearchResponse response;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.eee("getSearchHitArray IOException, searchRequest:%s", e, searchRequest.toString());
        }
        return parseSearchHitArray(response);
    }

    /**
     * ??????,??????/?????? ???JSON??????
     *
     * @param entity ??????
     * @return json
     */
    private String buildJsonIndexSource(T entity) {
        // ?????????????????????????????????
        // ??????????????????????????? ???????????????????????????es???????????????
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

        // ???????????????
        List<SerializeFilter> serializeFilters = new ArrayList<>();
        Optional.ofNullable(entityInfo.getClassSimplePropertyPreFilterMap().get(entityClass))
                .ifPresent(serializeFilters::addAll);

        // ??????????????????????????????
        SimplePropertyPreFilter simplePropertyPreFilter = FastJsonUtils.getSimplePropertyPreFilter(entity.getClass(), excludeColumn);
        Optional.ofNullable(simplePropertyPreFilter).ifPresent(serializeFilters::add);

        return JSON.toJSONString(entity, serializeFilters.toArray(new SerializeFilter[0]), SerializerFeature.WriteMapNullValue);
    }

    /**
     * ?????????????????????json
     *
     * @param updateWrapper ??????
     * @return json
     */
    private String buildJsonDoc(LambdaEsUpdateWrapper<T> updateWrapper) {
        List<EsUpdateParam> updateParamList = updateWrapper.updateParamList;
        JSONObject jsonObject = new JSONObject();

        updateParamList.forEach(esUpdateParam -> {
            String realField = FieldUtils.getRealFieldNotConvertId(esUpdateParam.getField(),
                    EntityInfoHelper.getEntityInfo(entityClass).getMappingColumnMap(),
                    GlobalConfigCache.getGlobalConfig().getDbConfig());
            jsonObject.put(realField, esUpdateParam.getValue());
        });
        return JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }

    /**
     * ??????bulk??????,?????????????????????
     *
     * @param bulkRequest    ??????????????????
     * @param requestOptions ??????
     * @return ????????????
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
     * ??????bulk????????????,?????????????????????,??????id
     *
     * @param bulkRequest    ??????????????????
     * @param requestOptions ??????
     * @param entityList     ????????????
     * @return ????????????
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
     * ???ES????????????????????????SearchHit[]
     *
     * @param searchResponse es??????????????????
     * @return ???????????????Hit??????
     */
    private SearchHit[] parseSearchHitArray(SearchResponse searchResponse) {
        return Optional.ofNullable(searchResponse)
                .map(SearchResponse::getHits)
                .map(SearchHits::getHits)
                .orElseThrow(() -> ExceptionUtils.eee("parseSearchHitArray exception"));
    }

    /**
     * ??????????????????
     *
     * @return ????????????
     */
    private String getIndexName(String indexName) {
        // ?????????wrapper?????????????????????,????????????????????????????????????????????????
        if (StringUtils.isBlank(indexName)) {
            return EntityInfoHelper.getEntityInfo(entityClass).getIndexName();
        }
        return indexName;
    }

    /**
     * ??????id??????????????????
     *
     * @return id??????????????????
     */
    private String getRealIdFieldName() {
        return EntityInfoHelper.getEntityInfo(entityClass).getKeyProperty();
    }

    /**
     * ???????????????->?????????????????? ?????????
     *
     * @return ?????????->?????????????????? map
     */
    private Map<String, String> getHighlightFieldMap() {
        return EntityInfoHelper.getEntityInfo(entityClass).getHighlightFieldMap();
    }

    /**
     * ????????????????????????
     *
     * @param entity         ?????????
     * @param highlightField ??????????????????
     * @param value          ???????????????
     */
    private void setHighlightValue(T entity, String highlightField, String value) {
        try {
            Method invokeMethod = BaseCache.setterMethod(entityClass, highlightField);
            invokeMethod.invoke(entity, value);
        } catch (Throwable e) {
            LogUtils.error("setHighlightValue error,entity:{},highlightField:{},value:{},e:{}",
                    entity.toString(), highlightField, value, e.toString());
        }
    }

    /**
     * ??????id???
     *
     * @param entity ??????
     * @param id     ??????
     */
    private void setId(Object entity, String id) {
        try {
            Method invokeMethod = BaseCache.setterMethod(entity.getClass(), getRealIdFieldName());

            // ???es?????????String??????id????????????????????????id??????,??????Long,?????????????????????
            Class<?> idClass = EntityInfoHelper.getEntityInfo(entity.getClass()).getIdClass();
            Object val = ReflectionKit.getVal(id, idClass);
            invokeMethod.invoke(entity, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????id???
     *
     * @param entity ????????????
     * @return id???
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
     * ?????????????????????????????????????????????CountDSL??????
     *
     * @param countRequest ????????????????????????
     */
    private void printCountDSL(CountRequest countRequest) {
        if (GlobalConfigCache.getGlobalConfig().isPrintDsl() && Objects.nonNull(countRequest)) {
            Optional.ofNullable(countRequest.query())
                    .ifPresent(source -> LogUtils.info(BaseEsConstants.COUNT_DSL_PREFIX + source));
        }
    }

    /**
     * ?????????????????????????????????????????????DSL??????
     *
     * @param searchRequest es??????????????????
     */
    private void printDSL(SearchRequest searchRequest) {
        if (GlobalConfigCache.getGlobalConfig().isPrintDsl() && Objects.nonNull(searchRequest)) {
            Optional.ofNullable(searchRequest.source())
                    .ifPresent(source -> LogUtils.info(BaseEsConstants.DSL_PREFIX + source));
        }
    }

    /**
     * ??????????????????
     *
     * @return ????????????
     */
    private String getRefreshPolicy() {
        return GlobalConfigCache.getGlobalConfig().getDbConfig().getRefreshPolicy().getValue();
    }

    /**
     * ??????
     *
     * @param entity         ????????????
     * @param joinFieldClass join?????????
     * @return
     */
    private String getRouting(T entity, Class<?> joinFieldClass) {
        // ????????????-?????????,????????????
        Method getJoinFieldMethod = BaseCache.getterMethod(entityClass, joinFieldClass.getSimpleName());
        Method getParentMethod = BaseCache.getterMethod(joinFieldClass, PARENT);
        try {
            Object joinField = getJoinFieldMethod.invoke(entity);
            Object parent = getParentMethod.invoke(joinField);
            return parent.toString();
        } catch (Throwable e) {
            LogUtils.error("build IndexRequest: child routing invoke error, joinFieldClass:{},entity:{},e:{}",
                    joinFieldClass.toString(), entity.toString(), e.toString());
            throw ExceptionUtils.eee("getRouting error", e);
        }
    }

}
