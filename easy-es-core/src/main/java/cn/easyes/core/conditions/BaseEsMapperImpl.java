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
        // 初始化创建索引参数
        CreateIndexParam createIndexParam = new CreateIndexParam();
        createIndexParam.setIndexName(wrapper.indexName);

        // 设置分片个副本信息
        Optional.ofNullable(wrapper.shardsNum).ifPresent(createIndexParam::setShardsNum);
        Optional.ofNullable(wrapper.replicasNum).ifPresent(createIndexParam::setReplicasNum);

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
        return IndexUtils.createIndex(client, EntityInfoHelper.getEntityInfo(entityClass), createIndexParam);
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
            Map<String, Object> mapping = IndexUtils.initMapping(EntityInfoHelper.getEntityInfo(entityClass), wrapper.esIndexParamList);
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
        // 执行普通混合查询, 不含searchAfter分页
        return getSearchResponse(wrapper, null);
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
        // 获取由本框架生成的es查询参数 用于验证生成语法的正确性
        SearchRequest searchRequest = new SearchRequest(getIndexName(wrapper.indexName));
        SearchSourceBuilder searchSourceBuilder = WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
        searchRequest.source(searchSourceBuilder);
        return Optional.ofNullable(searchRequest.source())
                .map(SearchSourceBuilder::toString)
                .orElseThrow(() -> ExceptionUtils.eee("get search source exception"));
    }

    @Override
    public PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize) {
        // 兼容分页参数
        pageNum = pageNum == null || pageNum <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_NUM : pageNum;
        pageSize = pageSize == null || pageSize <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;

        wrapper.from((pageNum - 1) * pageSize);
        wrapper.size(pageSize);

        // 请求es获取数据
        SearchResponse response = getSearchResponse(wrapper);

        // 解析数据
        SearchHit[] searchHits = parseSearchHitArray(response);
        List<T> dataList = Arrays.stream(searchHits).map(searchHit -> parseOne(searchHit, wrapper))
                .collect(Collectors.toList());
        long count = parseCount(response, Objects.nonNull(wrapper.distinctField));
        return PageHelper.getPageInfo(dataList, count, pageNum, pageSize);
    }

    @Override
    public SAPageInfo<T> searchAfterPage(LambdaEsQueryWrapper<T> wrapper, List<Object> searchAfter, Integer pageSize) {
        // searchAfter语法规定 或from只允许为0或-1或不传,否则es会报错, 推荐不指定, 直接传null即可
        boolean illegalArg = Objects.nonNull(wrapper.from) && (!wrapper.from.equals(ZERO) || !wrapper.from.equals(MINUS_ONE));
        if (illegalArg) {
            throw ExceptionUtils.eee("The wrapper.from in searchAfter must be 0 or -1 or null, null is recommended");
        }

        // searchAfter必须要进行排序，不排序无法进行分页
        if (CollectionUtils.isEmpty(wrapper.sortParamList)) {
            throw ExceptionUtils.eee("sortParamList cannot be empty");
        }

        // 兼容分页参数
        pageSize = pageSize == null || pageSize <= BaseEsConstants.ZERO ? BaseEsConstants.PAGE_SIZE : pageSize;
        wrapper.size(pageSize);

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
    public Long selectCount(LambdaEsQueryWrapper<T> wrapper, boolean distinct) {
        // 只查id列,节省内存
        wrapper.select(DEFAULT_ES_ID_NAME);

        if (distinct) {
            // 去重, 总数来源于桶
            SearchResponse response = getSearchResponse(wrapper);
            return parseCount(response, Objects.nonNull(wrapper.distinctField));
        } else {
            // 不去重,直接count获取,效率更高
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

        // 构建请求入参
        IndexRequest indexRequest = buildIndexRequest(entity, indexName);
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

    @Override
    public Integer insertBatch(Collection<T> entityList) {
        return insertBatch(entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer insertBatch(Collection<T> entityList, String indexName) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }
        // 构建批量请求参数
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        entityList.forEach(entity -> {
            IndexRequest indexRequest = buildIndexRequest(entity, indexName);
            bulkRequest.add(indexRequest);
        });
        // 执行批量请求并返回结果
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
        // 只查id列,节省内存
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

                    // 父子类型-子文档,追加其路由,否则无法删除
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
        // 获取id值
        String idValue = getIdValue(entity);

        // 构建更新请求参数
        UpdateRequest updateRequest = buildUpdateRequest(entity, idValue, indexName);
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

    @Override
    public Integer updateBatchByIds(Collection<T> entityList) {
        return updateBatchByIds(entityList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public Integer updateBatchByIds(Collection<T> entityList, String indexName) {
        if (CollectionUtils.isEmpty(entityList)) {
            return BaseEsConstants.ZERO;
        }

        // 封装批量请求参数
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());
        entityList.forEach(entity -> {
            String idValue = getIdValue(entity);
            UpdateRequest updateRequest = buildUpdateRequest(entity, idValue, indexName);
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

        // 查询数据列表
        List<T> list = selectListByUpdateWrapper(updateWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return BaseEsConstants.ZERO;
        }

        // 获取更新文档内容
        String jsonData = Optional.ofNullable(entity)
                .map(this::buildJsonIndexSource)
                .orElseGet(() -> buildJsonDoc(updateWrapper));

        // 批量更新
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

            // 父子类型-子文档,追加其路由,否则无法更新
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

        // 构造查询参数
        SearchRequest searchRequest = new SearchRequest(getIndexName(indexName));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(DEFAULT_ES_ID_NAME, id));
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
        return selectBatchIds(idList, EntityInfoHelper.getEntityInfo(entityClass).getIndexName());
    }

    @Override
    public List<T> selectBatchIds(Collection<? extends Serializable> idList, String indexName) {
        if (CollectionUtils.isEmpty(idList)) {
            throw ExceptionUtils.eee("id collection must not be null or empty");
        }

        // 构造查询参数
        List<String> stringIdList = idList.stream().map(Object::toString).collect(Collectors.toList());
        SearchRequest searchRequest = new SearchRequest(getIndexName(indexName));
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
    public T selectOne(LambdaEsQueryWrapper<T> wrapper) {
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

    @Override
    public Boolean setCurrentActiveIndex(String indexName) {
        synchronized (this) {
            EntityInfoHelper.getEntityInfo(entityClass).setIndexName(indexName);
        }
        return Boolean.TRUE;
    }

    /**
     * 获取es查询结果返回体
     *
     * @param wrapper     条件
     * @param searchAfter searchAfter参数
     * @return es返回体
     */
    private SearchResponse getSearchResponse(LambdaEsQueryWrapper<T> wrapper, Object[] searchAfter) {
        // 构建es restHighLevel 查询参数
        SearchRequest searchRequest = new SearchRequest(getIndexName(wrapper.indexName));
        SearchSourceBuilder searchSourceBuilder = WrapperProcessor.buildSearchSourceBuilder(wrapper, entityClass);
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
        deleteRequest.index(getIndexName(indexName));
        return deleteRequest;
    }

    /**
     * 查询数据列表
     *
     * @param wrapper 查询参数
     * @return 数据列表
     */
    private List<T> selectListByUpdateWrapper(LambdaEsUpdateWrapper<T> wrapper) {
        // 构建查询条件
        SearchRequest searchRequest = new SearchRequest(getIndexName(wrapper.indexName));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 只查id列,节省内存
        searchSourceBuilder.fetchSource(DEFAULT_ES_ID_NAME, null);
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.size(GlobalConfigCache.getGlobalConfig().getDbConfig().getBatchUpdateThreshold());
        BoolQueryBuilder boolQueryBuilder = WrapperProcessor.initBoolQueryBuilder(wrapper.baseEsParamList,
                wrapper.enableMust2Filter, entityClass);
        Optional.ofNullable(wrapper.geoParam).ifPresent(geoParam -> WrapperProcessor.setGeoQuery(geoParam, boolQueryBuilder, entityClass));
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        printDSL(searchRequest);
        try {
            //  查询数据明细
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

        indexName = StringUtils.isBlank(indexName) ? entityInfo.getIndexName() : indexName;
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
        updateRequest.index(getIndexName(indexName));
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
    private T parseOne(SearchHit searchHit, LambdaEsQueryWrapper<T> wrapper) {
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
        String distanceField = entityInfo.getDistanceField();
        if (Objects.isNull(distanceField) || ArrayUtils.isEmpty(sortValues)) {
            return;
        }

        try {
            // 有多个排序字段时,index为距离排序在sortBuilders中的位置
            Object sortValue = sortValues[entityInfo.getSortBuilderIndex()];
            if (!(sortValue instanceof Double)) {
                return;
            }
            double distance = (double) sortValue;
            if (Double.isNaN(distance)) {
                return;
            }
            if (entityInfo.getScoreDecimalPlaces() > ZERO) {
                distance = NumericUtils.setDecimalPlaces(distance, entityInfo.getDistanceDecimalPlaces());
            }
            Method invokeMethod = BaseCache.setterMethod(entity.getClass(), distanceField);
            invokeMethod.invoke(entity, distance);
        } catch (Throwable e) {
            // 遇到异常只提示, 不阻断流程 distance未设置不影核心业务
            LogUtils.formatError("set distance error, entity:{},sortValues:{},distanceField:{},e:{}", entity, JSON.toJSONString(sortValues), distanceField, e);
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
            LogUtils.formatError("set score error, entity:{},score:{},scoreField:{},e:{}", entity, score, scoreField, e);
        }
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
            throw ExceptionUtils.eee("getSearchHitArray exception,searchRequest:%s", e, searchRequest.toString());
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

        return JSON.toJSONString(entity, serializeFilters.toArray(new SerializeFilter[0]), SerializerFeature.WriteMapNullValue);
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

        updateParamList.forEach(esUpdateParam -> {
            String realField = FieldUtils.getRealFieldNotConvertId(esUpdateParam.getField(),
                    EntityInfoHelper.getEntityInfo(entityClass).getMappingColumnMap(),
                    GlobalConfigCache.getGlobalConfig().getDbConfig());
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
     * 获取索引名称
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
            LogUtils.error("setHighlightValue error,entity:{},highlightField:{},value:{},e:{}",
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
                    .ifPresent(source -> LogUtils.info(BaseEsConstants.COUNT_DSL_PREFIX + source));
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
                    .ifPresent(source -> LogUtils.info(BaseEsConstants.DSL_PREFIX + source));
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
     * 父子
     *
     * @param entity         实体对象
     * @param joinFieldClass join字段类
     * @return
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
            LogUtils.error("build IndexRequest: child routing invoke error, joinFieldClass:{},entity:{},e:{}",
                    joinFieldClass.toString(), entity.toString(), e.toString());
            throw ExceptionUtils.eee("getRouting error", e);
        }
    }

}
